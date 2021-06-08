import {firestore, initializeApp} from "firebase-admin";
import * as functions from "firebase-functions";
initializeApp();

export const newMessage = functions.firestore
    .document("Chats/{chatId}/Messages/{messageId}")
    .onWrite(async (change, ctx1) => {
      const parentRef = change.after.ref.parent.parent;
      const message = change.after.data()?.message;
      const sender = change.after.data()?.sender;
      const timeStamp = change.after.data()?.time_stamp;
      const parentDoc = await parentRef?.get();
      if (parentDoc?.exists) {
        const partyOne = parentDoc.data()?.party_one;
        const partyTwo = parentDoc.data()?.party_two;
        const data1 = {
          "party_id": partyTwo,
          "message": message,
          "sender": sender,
          "time_stamp": timeStamp,
        };
        const data2 = {
          "party_id": partyOne,
          "message": message,
          "sender": sender,
          "time_stamp": timeStamp,
        };
        await firestore().doc(`Users/${partyOne}/Chats/${ctx1.params.chatId}`)
            .set(data1);
        await firestore().doc(`Users/${partyTwo}/Chats/${ctx1.params.chatId}`)
            .set(data2);
      }
    });

// // Start writing Firebase Functions
// // https://firebase.google.com/docs/functions/typescript
//
// export const helloWorld = functions.https.onRequest((request, response) => {
//   functions.logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });
