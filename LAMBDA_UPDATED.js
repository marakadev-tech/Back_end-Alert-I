import { GoogleAuth } from "google-auth-library";
import fetch from "node-fetch";

export const handler = async (event) => {
  try {
    const serviceAccount = {
      project_id: process.env.FIREBASE_PROJECT_ID,
      client_email: process.env.FIREBASE_CLIENT_EMAIL,
      private_key: (process.env.FIREBASE_PRIVATE_KEY || "").replace(/\\n/g, "\n"),
    };

    const auth = new GoogleAuth({
      credentials: serviceAccount,
      scopes: ["https://www.googleapis.com/auth/firebase.messaging"],
    });

    const client = await auth.getClient();
    const token = await client.getAccessToken();

    const fcmUrl = `https://fcm.googleapis.com/v1/projects/${serviceAccount.project_id}/messages:send`;

    const body = JSON.parse(event.body || "{}");

    // Gérer plusieurs tokens ou un seul token
    const tokens = body.tokens || (body.token ? [body.token] : []);
    
    if (tokens.length === 0) {
      return {
        statusCode: 400,
        body: JSON.stringify({ 
          error: "No tokens provided",
          message: "Either 'token' or 'tokens' field is required"
        }),
      };
    }

    const results = [];
    let successCount = 0;
    let errorCount = 0;

    // Envoyer à chaque token
    for (const fcmToken of tokens) {
      try {
        const payload = {
          message: {
            token: fcmToken,
            notification: {
              title: body.title || "Hello from Lambda",
              body: body.body || "Test FCM via HTTP v1",
            },
            // Ajouter les données si présentes
            data: body.data || {}
          },
        };

        const response = await fetch(fcmUrl, {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token.token}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify(payload),
        });

        const data = await response.json();

        results.push({
          token: fcmToken.substring(0, 10) + "...", // Masquer le token complet
          status: response.status,
          success: response.ok,
          data: data
        });

        if (response.ok) {
          successCount++;
        } else {
          errorCount++;
        }

      } catch (tokenError) {
        console.error(`Error sending to token ${fcmToken.substring(0, 10)}...:`, tokenError);
        results.push({
          token: fcmToken.substring(0, 10) + "...",
          status: 500,
          success: false,
          error: tokenError.message
        });
        errorCount++;
      }
    }

    // Retourner le résultat global
    return {
      statusCode: errorCount === 0 ? 200 : 207, // 207 = Multi-Status (certains succès, certains échecs)
      body: JSON.stringify({
        success: successCount > 0,
        totalTokens: tokens.length,
        successCount: successCount,
        errorCount: errorCount,
        results: results,
        message: `Sent to ${successCount}/${tokens.length} tokens successfully`
      }),
    };

  } catch (error) {
    console.error("Lambda Error:", error);
    return {
      statusCode: 500,
      body: JSON.stringify({ 
        error: error.message,
        message: "Internal server error"
      }),
    };
  }
};
