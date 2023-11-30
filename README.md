# GuardianAngelTeenApp

## Overview

guardianAngelTeen App is an Android application aimed at providing parents with an effective and versatile tool for ensuring the safety and well-being of their teenage children. This application introduces several key features to help parents stay informed and take action when necessary. These features include Speed Monitoring, Geofencing, Fall Detection, and Health Vitals Monitoring. guardianTeenApp also includes an SOS button for immediate communication during emergencies and live location sharing to keep parents informed about their child's whereabouts.

## Project Components

The guardianTeenApp consists of several key components:

1. **ChildScreenActivity**: Responsible for monitoring the child's speed and health vitals. It utilizes GPS technology to track speed and mobile sensors for health monitoring.

2. **HealthRateCalculator**: This class calculates health data, including heart rate and respiratory rate. It uses data from the device's camera to calculate heart rate and accelerometer data for respiratory rate.

3. **HealthDataRepository**: Manages the storage and retrieval of health data, including heart rate and respiratory rate.

4. **AlertFunction and VibrateFunction**: These components are responsible for triggering alerts and vibrations in predefined scenarios, ensuring prompt communication during emergencies.

## Execution Instructions

To run the guardianTeenApp Android application, follow these steps:

1. Clone the project repository from https://github.com/Samarth66/guardianTeenApp/tree/Prajjwal_Pandey.

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
