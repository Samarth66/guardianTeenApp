#####NOTE : If you are running the app the first time round, on initial sign-in it will say network error, keep trying, it will log-in in under 2-3 minutes (apologies for the inconvenience)

# GuardianTeenApp

## Overview

guardianAngelTeen App is an Android application aimed at providing parents with an effective and versatile tool for ensuring the safety and well-being of their teenage children. This application introduces several key features to help parents stay informed and take action when necessary. These features include Speed Monitoring, Geofencing, Fall Detection, and Health Vitals Monitoring. guardianTeenApp also includes an SOS button for immediate communication during emergencies and live location sharing to keep parents informed about their child's whereabouts.

## Project Components

The guardianTeenApp consists of several key components:

1. **ChildScreenActivity**: Implemented the features for free fall detection and SOS emergency button with optional video recording

2. **SOS Alert**: Sends an alert to the respective parent as type "SOS", and allows the child to record a 30 second video in case of emergencies

3. **Fall Detection**: Uses the device accelerometer to detect free falls using a threshold value (computed through trial and error) and sends a notification alert to the respective parent.

4. **AlertFunction and VibrateFunction**: These components are responsible for triggering alerts and vibrations in predefined scenarios, ensuring prompt communication during emergencies.

## Execution Instructions

To run the guardianTeenApp Android application, follow these steps:

1. Clone the project repository from https://github.com/Samarth66/guardianTeenApp/tree/Deepak_Nayani.

2. Open the project in Android Studio.

3. Build and run the application on an Android emulator or a physical Android device.

4. Grant the necessary permissions for location, camera, and sensors when prompted.

5. Navigate through the application to access its features, including speed monitoring, health vitals, and emergency functions.

6. Refer to the in-app user guide for detailed instructions on using each feature.

## Additional Notes

- Make sure your Android development environment is set up correctly with the required SDK and dependencies.

- The application may require additional setup, such as API key configurations or device permissions, depending on the development environment and target Android version.

- This README provides a high-level overview of the project and basic execution instructions. Detailed documentation, including code structure, architecture, and usage guidelines, can be found within the project source code and associated documentation files.

Thank you for using guardianAngelTeen App, and we hope this application enhances the safety and well-being of your teenage children.
