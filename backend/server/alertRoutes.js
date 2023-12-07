const admin = require("firebase-admin");
const express = require("express");
const router = express.Router();
const Alert = require("./alertSchema"); // Import your Alert model
const User = require("./userSchema"); // Import your User model
const Relationship = require("./relationshipSchema");
const LocationModel = require("./locationSchema");
const { Client } = require("@googlemaps/google-maps-services-js");
const client = new Client({});

// Create an alert

router.post("/create", async (req, res) => {
  const { cid, type, description, time } = req.body;
  const locationData = await LocationModel.findOne({ cid: cid });
  console.log("Received cid", cid);
  console.log("fetched location data", locationData);
  const [longitude, latitude] = locationData.coordinates;
  const locationString = `${latitude}, ${longitude}`;
  console.log(locationString);

  try {
    const findNearestPlaces = async (type) => {
      const response = await client.placesNearby({
        params: {
          location: { latitude, longitude },
          radius: 10000, // search within 1km radius
          type: type,
          key: "AIzaSyAYna2V-xNgIEr0_cIjlUFMHbKmASUK5NA",
        },
      });
      console.log("Google Maps API Response:", response);

      return response.data.results[0];
    };

    const nearestPoliceStation = await findNearestPlaces("police");
    const nearestHospital = await findNearestPlaces("hospital");

    console.log(nearestPoliceStation);
    console.log(nearestHospital);
    const updatedDescription = `${description}. Nearest Police Station: ${nearestPoliceStation.name} Nearest Hospital: ${nearestHospital.name}`;

    const newAlert = new Alert({
      cid,
      type,
      description: updatedDescription,
      time,
      location: locationString,
    });
    await newAlert.save();

    // Step 1: Identify the Parent's ID
    const relationship = await Relationship.findOne({ cid: cid });
    if (!relationship) {
      throw new Error("Parent-child relationship not found");
    }
    const pid = relationship.pid;
    console.log(pid, "parentId");

    // Step 2: Get the Parent's Device Token
    const parentUser = await User.findById(pid);
    if (!parentUser || !parentUser.deviceToken) {
      throw new Error("Parent user or device token not found");
    }
    const parentDeviceToken = parentUser.deviceToken;
    console.log("tockenID", parentDeviceToken);

    // Step 3: Send a Push Notification
    const message = {
      notification: {
        title: "New Alert",
        body: `New alert: ${type} detected.`,
      },
      token: parentDeviceToken,
    };

    admin
      .messaging()
      .send(message)
      .then((response) => {
        console.log("Successfully sent message:", response);
      })
      .catch((error) => {
        console.log("Error sending message:", error);
      });

    res.status(201).json({
      message: "Alert created successfully",
      alert: newAlert,
    });
  } catch (error) {
    console.error("Create Alert error:", error);
    res.status(500).json({ error: error.message });
  }
});
router.get("/fetch-alerts", async (req, res) => {
  const { pid } = req.query; // Assuming pid is passed as a query parameter

  try {
    // Step 1: Fetch the cid for the given pid from relationshipSchema
    const relationship = await Relationship.findOne({ pid: pid });
    if (!relationship) {
      return res.status(404).json({ message: "Relationship not found" });
    }
    const cid = relationship.cid;

    // Step 2: Fetch all alerts for the cid
    const alerts = await Alert.find({ cid: cid });
    if (!alerts || alerts.length === 0) {
      return res
        .status(404)
        .json({ message: "No alerts found for this child" });
    }

    res.status(200).json({
      message: "Alerts fetched successfully",
      alerts: alerts,
    });
  } catch (error) {
    console.error("Fetch Alerts error:", error);
    res.status(500).json({ error: error.message });
  }
});

router.post("/health-alert", async (req, res) => {
  const { pid } = req.body;
  try {
    const relationship = await Relationship.findOne({ pid: pid });
    if (!relationship) {
      return res.status(404).json({ message: "Relationship not found" });
    }
    const cid = relationship.cid;
    console.log(cid);
    const locationData = await LocationModel.findOne({ cid: cid });
    console.log(locationData);
    const [longitude, latitude] = locationData.coordinates;
    const locationString = `${latitude}, ${longitude}`;

    const { type, description, time, location } = req.body;
    const healthAlertData = { cid, type, description, time, location };

    const newHealthAlert = new Alert(healthAlertData);
    await newHealthAlert.save();

    const parentUser = await User.findById(cid);
    if (!parentUser || !parentUser.deviceToken) {
      throw new Error("Parent user or device token not found");
    }
    const parentDeviceToken = parentUser.deviceToken;
    console.log(parentDeviceToken);
    // Send a Push Notification
    const message = {
      notification: {
        title: "New Health Alert",
        body: `New health alert of ${type} for your child.`,
      },
      token: parentDeviceToken,
    };

    admin
      .messaging()
      .send(message)
      .then((response) => {
        console.log("Successfully sent message:", response);
      })
      .catch((error) => {
        console.log("Error sending message:", error);
      });

    res.status(201).json({
      message: "Health alert created successfully",
      healthAlert: newHealthAlert,
    });
  } catch (error) {
    console.error("Health Alert error:", error);
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
