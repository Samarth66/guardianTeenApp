# guardianTeenApp
## Overview
The GuardianAngelTeenApp is an Android application designed to empower parents with a versatile tool to ensure the safety and well-being of their teenage children. This app introduces a range of essential features to help parents stay informed and take proactive measures when needed. These functionalities encompass Speed Monitoring, Geofencing, Fall Detection, and Health Vitals Monitoring. Additionally, the app includes an SOS button for immediate emergency communication and live location sharing to keep parents updated on their child's whereabouts.

## Execution Instructions
To run the GuardianAngelTeenApp Android application, follow these steps:

1. Clone the project repository from https://github.com/Samarth66/guardianTeenApp.git

2. Open the project using Android Studio.

3. Build and launch the application either on an Android emulator or a physical Android device.

4. Grant the necessary permissions, specially Notification permissions to test notifications

5. Ensure that you check the following functionalities:
   - Logging in and signing up to enable communication.
   - Adding a childId in parent screen
   - Sending alerts notification from the child screen to parent device by clicking on sendAlert button in child screen.
   - Sending alert from parent device to child device by clicking health vital check button.

## Backend Deployment

The backend of this project is already deployed on https://render.com). You can directly install the app using above instructions and test the endpoints using the app. However, if you want to deploy the backend code on your own laptop locally, follow these steps:

1. Clone the repository.
2. Open the project in Visual Studio.
3. Navigate to the server folder.
4. Run `npm install` to install all dependencies.
5. Execute `node app` or `node app.js` to start the backend server.

## Android Frontend Changes

To update the Android frontend with the new API endpoints, follow these steps:

1. Replace all instances of the following example format API endpoint:
   - `https://guardianteenbackend.onrender.com/signup`

   with:

   - `http://192.168.0.95:5001/login`
     (Replace the middle part with your IPv4 address, which can be found by running `ipconfig`)

   This will ensure that all requests are received on your laptop.

Make these endpoint updates in the following locations within the Android app:

- `https://guardianteenbackend.onrender.com/login` (Present in `loginActivity`)
- `https://guardianteenbackend.onrender.com/updateDeviceToken` (Present in `loginActivity`)
- `https://guardianteenbackend.onrender.com/create` (Present in `childActivity`)
- `https://guardianteenbackend.onrender.com/health-alert` (In `ParentActivity`)
- `https://guardianteenbackend.onrender.com/fetch-alerts?pid=$parentId` (Present in `parentActivity`)
- `https://guardianteenbackend.onrender.com/add_child` (Present in `parentActivity`)
- `https://guardianteenbackend.onrender.com/signup` (Present in `signupActivity`)

Feel free to reach out if you have any questions or need further assistance!
