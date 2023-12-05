<<<<<<< HEAD
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
=======
<<<<<<< HEAD

# GuardianAngelTeenApp

=======
#####NOTE : If you are running the app the first time round, on initial sign-in it will say network error, keep trying, it will log-in in under 2-3 minutes (apologies for the inconvenience)

# GuardianTeenApp

> > > > > > > origin/Deepak_Nayani

## Overview

guardianAngelTeen App is an Android application aimed at providing parents with an effective and versatile tool for ensuring the safety and well-being of their teenage children. This application introduces several key features to help parents stay informed and take action when necessary. These features include Speed Monitoring, Geofencing, Fall Detection, and Health Vitals Monitoring. guardianTeenApp also includes an SOS button for immediate communication during emergencies and live location sharing to keep parents informed about their child's whereabouts.

## Project Components

The guardianTeenApp consists of several key components:

<<<<<<< HEAD

1. **ChildScreenActivity**: Responsible for monitoring the child's speed and health vitals. It utilizes GPS technology to track speed and mobile sensors for health monitoring.

2. **HealthRateCalculator**: This class calculates health data, including heart rate and respiratory rate. It uses data from the device's camera to calculate heart rate and accelerometer data for respiratory rate.

3. # **HealthDataRepository**: Manages the storage and retrieval of health data, including heart rate and respiratory rate.
4. **ChildScreenActivity**: Implemented the features for free fall detection and SOS emergency button with optional video recording

5. **SOS Alert**: Sends an alert to the respective parent as type "SOS", and allows the child to record a 30 second video in case of emergencies

6. **Fall Detection**: Uses the device accelerometer to detect free falls using a threshold value (computed through trial and error) and sends a notification alert to the respective parent.

   > > > > > > > origin/Deepak_Nayani

7. **AlertFunction and VibrateFunction**: These components are responsible for triggering alerts and vibrations in predefined scenarios, ensuring prompt communication during emergencies.

## Execution Instructions

To run the guardianTeenApp Android application, follow these steps:

<<<<<<< HEAD

1. # Clone the project repository from https://github.com/Samarth66/guardianTeenApp/tree/Prajjwal_Pandey.
1. Clone the project repository from https://github.com/Samarth66/guardianTeenApp/tree/Deepak_Nayani.

   > > > > > > > origin/Deepak_Nayani

1. Open the project in Android Studio.

1. Build and run the application on an Android emulator or a physical Android device.

1. Grant the necessary permissions for location, camera, and sensors when prompted.

1. Navigate through the application to access its features, including speed monitoring, health vitals, and emergency functions.

1. Refer to the in-app user guide for detailed instructions on using each feature.

## Additional Notes

- Make sure your Android development environment is set up correctly with the required SDK and dependencies.

- The application may require additional setup, such as API key configurations or device permissions, depending on the development environment and target Android version.

- This README provides a high-level overview of the project and basic execution instructions. Detailed documentation, including code structure, architecture, and usage guidelines, can be found within the project source code and associated documentation files.

Thank you for using guardianAngelTeen App, and we hope this application enhances the safety and well-being of your teenage children.
>>>>>>> origin/Deepak_Nayani
