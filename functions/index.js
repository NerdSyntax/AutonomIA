const functions = require("firebase-functions/v1");
const {onCall, HttpsError} = require("firebase-functions/v2/https");
const {onSchedule} = require("firebase-functions/v2/scheduler");
const {defineSecret, defineString} = require("firebase-functions/params");
const {initializeApp} = require("firebase-admin/app");
const {FieldValue, getFirestore} = require("firebase-admin/firestore");
const {getStorage} = require("firebase-admin/storage");
const {randomUUID} = require("node:crypto");

initializeApp();

const mpSandboxToken = defineSecret("MP_SANDBOX_ACCESS_TOKEN");
const mpSandboxFirebaseUid = defineString("MP_SANDBOX_FIREBASE_UID");
const mpSandboxPayerEmail = defineString("MP_SANDBOX_PAYER_EMAIL");

const sandboxSeller = "3830737395";
const sandboxPlusSubscription = "2c999664c99341d685047dec9930b682";
const sandboxPlusPlan = "3be5f667a82943afb2413cffb9195bba";
const prices = {PLUS: 4990, PRO: 12990};
const queueName = "sandboxCheckoutQueue";
const subscriptionIdPattern = /^[A-Za-z0-9_-]{1,128}$/;

exports.cleanupDeletedAccount = functions
    .runWith({failurePolicy: true})
    .auth.user()
    .onDelete(async ({uid}) => {
      if (typeof uid !== "string" || !/^[A-Za-z0-9_-]{1,128}$/.test(uid)) {
        throw new Error("UID no válido para la limpieza.");
      }

      const db = getFirestore();
      await db.recursiveDelete(db.collection("users").doc(uid));
      await db.recursiveDelete(db.collection("businesses").doc(uid));
      await getStorage().bucket().deleteFiles({prefix: `users/${uid}/`});
    });

async function mpSandboxGet(path) {
  try {
    const response = await fetch("https://api.mercadopago.com" + path, {
      headers: {Authorization: "Bearer " + mpSandboxToken.value().trim()},
      signal: AbortSignal.timeout(10000),
      redirect: "error",
    });
    if (!response.ok) throw new Error("Mercado Pago HTTP " + response.status);
    return await response.json();
  } catch (_) {
    throw new HttpsError("unavailable", "No pudimos consultar Mercado Pago.");
  }
}

async function hasApprovedCharge(subscriptionId, amount, currency) {
  const invoices = await mpSandboxGet(
      "/authorized_payments/search?preapproval_id=" + subscriptionId,
  );

  return Array.isArray(invoices?.results) && invoices.results.some((invoice) =>
    invoice.preapproval_id === subscriptionId &&
    invoice.currency_id === currency &&
    Number(invoice.transaction_amount) === amount &&
    invoice.payment?.status === "approved"
  );
}

// Conserva la prueba anterior. La app ya no llama a esta función.
exports.syncSandboxPlus = onCall({secrets: [mpSandboxToken]}, async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Inicia sesión en AutonomIA.");
  }

  const uid = request.auth.uid;
  if (uid !== mpSandboxFirebaseUid.value().trim()) {
    throw new HttpsError("permission-denied", "Esta prueba no está habilitada para tu cuenta.");
  }

  const id = sandboxPlusSubscription;
  const sub = await mpSandboxGet("/preapproval/" + id);

  if (sub?.id !== id || sub.status !== "authorized" ||
      sub.preapproval_plan_id !== sandboxPlusPlan ||
      String(sub.collector_id) !== sandboxSeller ||
      String(sub.payer_id) !== "3830737397" ||
      sub.auto_recurring?.currency_id !== "CLP" ||
      Number(sub.auto_recurring?.transaction_amount) !== 4990) {
    throw new HttpsError("failed-precondition", "La suscripción Plus de prueba no coincide.");
  }

  if (!(await hasApprovedCharge(id, 4990, "CLP"))) {
    throw new HttpsError("failed-precondition", "Todavía no hay un cobro Plus aprobado.");
  }

  const db = getFirestore();
  const user = db.collection("users").doc(uid);
  const profile = user.collection("business").doc("profile");
  const billing = user.collection("billing").doc("current");

  await db.runTransaction(async (tx) => {
    const [profileDoc, billingDoc] = await tx.getAll(profile, billing);

    if (!profileDoc.exists) {
      throw new HttpsError("failed-precondition", "Completa el perfil del negocio primero.");
    }

    if (billingDoc.exists &&
        (billingDoc.get("plan") !== "PLUS" ||
         billingDoc.get("origen") !== "MERCADO_PAGO_SANDBOX" ||
         billingDoc.get("subscriptionId") !== id)) {
      throw new HttpsError("failed-precondition", "La cuenta ya tiene un plan registrado.");
    }

    if (!billingDoc.exists) {
      tx.set(billing, {
        plan: "PLUS",
        origen: "MERCADO_PAGO_SANDBOX",
        subscriptionId: id,
      });
    }
  });

  return {plan: "PLUS"};
});

exports.createSandboxCheckout = onCall({
  region: "us-central1",
  invoker: "public",
  secrets: [mpSandboxToken],
}, async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Inicia sesión en AutonomIA.");
  }

  const plan = request.data?.plan;
  if (plan !== "PLUS" && plan !== "PRO") {
    throw new HttpsError("invalid-argument", "Selecciona Plus o Pro.");
  }

  const payerEmail = mpSandboxPayerEmail.value().trim();
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(payerEmail)) {
    throw new HttpsError("failed-precondition", "Falta el correo del comprador de prueba.");
  }

  const uid = request.auth.uid;
  const amount = prices[plan];
  const currency = "CLP";
  const reference = randomUUID();

  const db = getFirestore();
  const user = db.collection("users").doc(uid);
  const profile = user.collection("business").doc("profile");
  const billing = user.collection("billing").doc("current");
  const checkout = user.collection("sandboxCheckouts").doc("current");
  const queue = db.collection(queueName);

  const previousUrl = await db.runTransaction(async (tx) => {
    const [profileDoc, billingDoc, checkoutDoc] = await tx.getAll(
        profile, billing, checkout,
    );

    if (!profileDoc.exists) {
      throw new HttpsError("failed-precondition", "Completa el perfil del negocio primero.");
    }

    if (billingDoc.exists && billingDoc.get("plan") !== "FREE") {
      throw new HttpsError("failed-precondition", "Tu cuenta ya tiene un plan de pago.");
    }

    if (checkoutDoc.exists && checkoutDoc.get("status") !== "cancelled") {
      if (checkoutDoc.get("plan") === plan && checkoutDoc.get("status") === "pending") {
        const oldReference = checkoutDoc.get("reference");
        const oldSubscription = checkoutDoc.get("subscriptionId");
        const oldUrl = checkoutDoc.get("checkoutUrl");

        if (typeof oldReference === "string" && /^[0-9a-f-]{36}$/.test(oldReference) &&
            typeof oldSubscription === "string" && subscriptionIdPattern.test(oldSubscription) &&
            typeof oldUrl === "string" && oldUrl) {
          const job = queue.doc(oldReference);
          if (!(await tx.get(job)).exists) {
            const now = Date.now();
            tx.set(job, {
              uid,
              subscriptionId: oldSubscription,
              createdAt: now,
              checkAt: now,
            });
          }
          return oldUrl;
        }
      }

      throw new HttpsError("failed-precondition", "Ya tienes una compra en curso.");
    }

    tx.set(checkout, {
      reference,
      plan,
      amount,
      currency,
      status: "creating",
      createdAt: FieldValue.serverTimestamp(),
    });
    return null;
  });

  if (previousUrl) return {checkoutUrl: previousUrl};

  let response;
  try {
    response = await fetch("https://api.mercadopago.com/preapproval", {
      method: "POST",
      redirect: "error",
      headers: {
        Authorization: "Bearer " + mpSandboxToken.value().trim(),
        "Content-Type": "application/json",
      },
      signal: AbortSignal.timeout(10000),
      body: JSON.stringify({
        reason: `AutonomIA ${plan === "PLUS" ? "Plus" : "Pro"} - Prueba`,
        external_reference: reference,
        payer_email: payerEmail,
        auto_recurring: {
          frequency: 1,
          frequency_type: "months",
          transaction_amount: amount,
          currency_id: currency,
        },
        back_url: "https://www.mercadopago.cl/",
        status: "pending",
      }),
    });
  } catch (_) {
    console.warn("Checkout: resultado de creación desconocido.", {reference});
    throw new HttpsError("unavailable", "No pudimos confirmar la creación del enlace.");
  }

  if (!response.ok) {
    if ([400, 401, 403, 422].includes(response.status)) {
      await db.runTransaction(async (tx) => {
        const doc = await tx.get(checkout);
        if (doc.get("reference") === reference && doc.get("status") === "creating") {
          tx.delete(checkout);
        }
      });
    }

    console.warn("Checkout: Mercado Pago rechazó la solicitud.", {
      reference,
      status: response.status,
    });
    throw new HttpsError("unavailable", "Mercado Pago no pudo crear el enlace.");
  }

  let sub;
  let url;
  try {
    sub = await response.json();
    url = new URL(sub.init_point);
  } catch (_) {
    throw new HttpsError("unavailable", "Mercado Pago no devolvió un enlace válido.");
  }

  if (typeof sub.id !== "string" || !subscriptionIdPattern.test(sub.id) ||
      sub.external_reference !== reference || sub.status !== "pending" ||
      String(sub.collector_id) !== sandboxSeller ||
      !["https://www.mercadopago.cl", "https://www.mercadopago.com"].includes(url.origin) ||
      url.username || url.password || url.pathname !== "/subscriptions/checkout" ||
      url.searchParams.get("preapproval_id") !== sub.id) {
    throw new HttpsError("unavailable", "Mercado Pago devolvió una compra inesperada.");
  }

  const checkoutUrl = url.toString();
  const now = Date.now();
  const batch = db.batch();

  batch.update(checkout, {
    status: "pending",
    subscriptionId: sub.id,
    checkoutUrl,
  });
  batch.set(queue.doc(reference), {
    uid,
    subscriptionId: sub.id,
    createdAt: now,
    checkAt: now,
  });

  await batch.commit();
  return {checkoutUrl};
});

exports.reconcileSandboxCheckouts = onSchedule({
  schedule: "every 2 minutes",
  region: "us-central1",
  secrets: [mpSandboxToken],
  maxInstances: 1,
  concurrency: 1,
  timeoutSeconds: 60,
}, async () => {
  const db = getFirestore();
  const due = await db.collection(queueName)
      .where("checkAt", "<=", Date.now())
      .orderBy("checkAt")
      .limit(10)
      .get();

  const results = await Promise.allSettled(due.docs.map(async (job) => {
    const {uid, subscriptionId, createdAt} = job.data();
    const user = db.collection("users").doc(uid);
    const checkout = user.collection("sandboxCheckouts").doc("current");
    const billing = user.collection("billing").doc("current");
    const profile = user.collection("business").doc("profile");
    const attempt = (await checkout.get()).data();

    if (attempt?.reference !== job.id ||
        attempt.subscriptionId !== subscriptionId ||
        attempt.status !== "pending") {
      await job.ref.delete();
      return;
    }

    const age = Date.now() - createdAt;
    const delay = age < 30 * 60000 ? 120000 : age < 86400000 ? 900000 : 7200000;
    await job.ref.update({checkAt: Date.now() + delay});

    const {plan, amount, currency, reference} = attempt;
    if ((plan !== "PLUS" && plan !== "PRO") || prices[plan] !== amount ||
        currency !== "CLP" || !subscriptionIdPattern.test(subscriptionId)) {
      return;
    }

    const sub = await mpSandboxGet("/preapproval/" + subscriptionId);
    if (sub?.id !== subscriptionId || sub.external_reference !== reference ||
        String(sub.collector_id) !== sandboxSeller ||
        sub.auto_recurring?.currency_id !== currency ||
        Number(sub.auto_recurring?.transaction_amount) !== amount) {
      return;
    }

    const cancelled = sub.status === "cancelled" || sub.status === "canceled";
    if (sub.status !== "authorized" && !cancelled) return;

    if (!cancelled && !(await hasApprovedCharge(subscriptionId, amount, currency))) {
      return;
    }

    await db.runTransaction(async (tx) => {
      const [activeJob, activeAttempt, current, currentProfile] = await tx.getAll(
          job.ref, checkout, billing, profile,
      );

      if (!activeJob.exists) return;
      if (activeAttempt.get("reference") !== reference ||
          activeAttempt.get("subscriptionId") !== subscriptionId ||
          activeAttempt.get("status") !== "pending") {
        tx.delete(job.ref);
        return;
      }
      if (!currentProfile.exists) return;

      if (cancelled) {
        tx.update(checkout, {status: "cancelled"});
      } else if (current.exists && current.get("plan") !== "FREE" &&
          (current.get("subscriptionId") !== subscriptionId || current.get("plan") !== plan)) {
        console.warn("Sandbox: ya existe otro plan para esta compra.", {reference});
        tx.update(checkout, {status: "conflict"});
      } else {
        tx.set(billing, {
          plan,
          origen: "MERCADO_PAGO_SANDBOX",
          subscriptionId,
          updatedAt: FieldValue.serverTimestamp(),
        });
        tx.update(checkout, {status: "paid"});
      }

      tx.delete(job.ref);
    });
  }));

  const failures = results.filter((result) => result.status === "rejected").length;
  if (failures) {
    console.warn("Sandbox: consultas pendientes de reintento.", {failures});
  }
});

exports.cancelSandboxSubscription = onCall({
  region: "us-central1",
  invoker: "public",
  secrets: [mpSandboxToken],
}, async (request) => {
  const uid = request.auth?.uid;
  if (!uid) throw new HttpsError("unauthenticated", "Inicia sesión en AutonomIA.");

  const db = getFirestore();
  const user = db.collection("users").doc(uid);
  const billing = user.collection("billing").doc("current");
  const checkout = user.collection("sandboxCheckouts").doc("current");
  const [bill, attempt] = await db.getAll(billing, checkout);
  const plan = bill.get("plan");
  const id = bill.get("subscriptionId");

  if (!bill.exists || plan === "FREE") return {cancelled: true};

  if ((plan !== "PLUS" && plan !== "PRO") ||
      bill.get("origen") !== "MERCADO_PAGO_SANDBOX" ||
      typeof id !== "string" || !subscriptionIdPattern.test(id)) {
    throw new HttpsError(
        "failed-precondition", "Este plan no tiene una suscripción de prueba vinculada.",
    );
  }

  if (request.data?.expectedSubscriptionId !== id) {
    throw new HttpsError("aborted", "La suscripción cambió. Vuelve a abrir Mi suscripción.");
  }

  const reference = attempt.get("reference");
  const matchesAttempt = (doc) => doc.exists &&
      doc.get("subscriptionId") === id && doc.get("reference") === reference &&
      doc.get("status") === "paid" && doc.get("plan") === plan;

  if (attempt.exists ?
      !matchesAttempt(attempt) || typeof reference !== "string" || !/^[0-9a-f-]{36}$/.test(reference) :
      id !== sandboxPlusSubscription) {
    throw new HttpsError("failed-precondition", "No se pudo verificar la compra vinculada.");
  }

  const path = "/preapproval/" + id;
  const matchesSubscription = (sub) => sub?.id === id &&
      String(sub.collector_id) === sandboxSeller &&
      (attempt.exists ? sub.external_reference === reference :
        sub.preapproval_plan_id === sandboxPlusPlan);
  const isCancelled = (sub) => sub?.status === "cancelled" || sub?.status === "canceled";
  let sub = await mpSandboxGet(path);

  if (!matchesSubscription(sub)) {
    throw new HttpsError("failed-precondition", "La suscripción no coincide con tu cuenta.");
  }

  if (!isCancelled(sub)) {
    try {
      const response = await fetch("https://api.mercadopago.com" + path, {
        method: "PUT",
        redirect: "error",
        headers: {
          Authorization: "Bearer " + mpSandboxToken.value().trim(),
          "Content-Type": "application/json",
        },
        body: JSON.stringify({status: "canceled"}),
        signal: AbortSignal.timeout(10000),
      });
      if (response.body) await response.body.cancel();
    } catch (_) {
      // Aunque la respuesta se pierda, Mercado Pago puede haber aplicado el cambio.
    }
    sub = await mpSandboxGet(path);
  }

  if (!matchesSubscription(sub) || !isCancelled(sub)) {
    throw new HttpsError(
        "unavailable", "Mercado Pago aún no confirma la cancelación. Intenta de nuevo.",
    );
  }

  await db.runTransaction(async (tx) => {
    const [current, latestAttempt] = await tx.getAll(billing, checkout);
    if (!current.exists || current.get("plan") === "FREE") return;
    if (current.get("subscriptionId") !== id || current.get("plan") !== plan ||
        current.get("origen") !== "MERCADO_PAGO_SANDBOX" ||
        (attempt.exists ? !matchesAttempt(latestAttempt) : latestAttempt.exists)) {
      throw new HttpsError("aborted", "El plan cambió mientras se gestionaba la cancelación.");
    }

    if (attempt.exists) {
      tx.delete(db.collection(queueName).doc(reference));
      tx.delete(checkout);
    }
    tx.delete(billing);
  });

  return {cancelled: true};
});