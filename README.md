# Snipe-Messenger
Snipe is a chat application for android built using Kotlin and Firestore. Snipe Uses clean architecture and jetpack components alongside Kotlin Coroutines and flows for a smooth experience and efficient functioning.

<p float="left">
<img src="Resources/login%20screen.png" alt="Login Screen" width="220"/>
<img src="Resources/registration%20screen.png" alt="Registration Screen" width="220"/>
<img src="Resources/chat%20screen.png" alt="Chat Screen" width="220"/>
<img src="Resources/message%20screen.png" alt="Message Screen" width="220"/>
</p>

#### App Setup Process
- Clone the repository
- Create new Firebase project, and link the cloned project with it (You can either do it manually or use Tools > Firebase Assistant )
- If the above step is done manually, make sure to copy-paste services.json file to your app directory

#### Cloud Functions Setup Process
- Open terminal and run ```npm install -g firebase-tools```
- Now login to Firebase CLI by running ```firebase login```
- Go to the Cloned Project directory and run ```firebase init firestore```, and follow on the screen steps to select the Firebase project you have just created
- To initialize your cloud functions, run ```firebase init functions```
  - When prompted to choose between Javascript and Typescript, select Typescript
  - When prompted to overwrite index.ts, select no
  
***Note***
- 🚧 🚧 Work in Progress - Some features may not work 🚧 🚧
- Cloud Functions requires Firebase Blaze plan for your project, to know more, visit [Firebase Pricing](https://firebase.google.com/pricing)
