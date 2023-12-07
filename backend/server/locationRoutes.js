const admin = require("firebase-admin");
const express = require("express");
const router = express.Router();
const Alert = require("./alertSchema"); // Import your Alert model
const User = require("./userSchema"); // Import your User model
const Relationship = require("./relationshipSchema");
const LocationModel = require("./locationSchema");

// Function to check if the child's coordinates are within the geofence
function isWithinGeofence(childCoordinates, geofenceCoordinates) {
  const [childLng, childLat] = childCoordinates;
  const [geofenceLng, geofenceLat] = geofenceCoordinates;

  const R = 6371e3; // metres
  const φ1 = (childLat * Math.PI) / 180; // φ, λ in radians
  const φ2 = (geofenceLat * Math.PI) / 180;
  const Δφ = ((geofenceLat - childLat) * Math.PI) / 180;
  const Δλ = ((geofenceLng - childLng) * Math.PI) / 180;

  const a =
    Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
    Math.cos(φ1) * Math.cos(φ2) * Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

  const distance = R * c; // in metres

  return distance <= 200; // 200 meters as the geofence radius
}

async function checkForGeofenceBreaches(cid) {
  try {
    const latestLocation = await LocationModel.findOne({ cid: cid });
    if (!latestLocation || !latestLocation.geofenceCordinates) {
      console.log("No location or geofence data found for cid:", cid);
      return;
    }

    const isInsideGeofence = isWithinGeofence(
      latestLocation.coordinates,
      latestLocation.geofenceCordinates
    );

    if (!isInsideGeofence) {
      console.log("not inside");
      if (latestLocation.lastGeofenceState == true) {
        console.log("true detected");
        // Child has just left the geofence, send notification
        await sendGeofenceNotification(cid);
        // Update the last geofence state to false (outside)
        await LocationModel.updateOne(
          { cid: cid },
          { $set: { lastGeofenceState: false } }
        );
      }
    } else {
      console.log("false detected, inside the geodence making it to true");
      // Update the last geofence state to true (inside)
      await LocationModel.updateOne(
        { cid: cid },
        { $set: { lastGeofenceState: true } }
      );
    }
  } catch (error) {
    console.error("Error in checkForGeofenceBreaches:", error);
  }
}

async function sendGeofenceNotification(cid) {
  console.log("Sending notification");
  // Fetch parent's device token
  const relationship = await Relationship.findOne({ cid: cid });
  if (!relationship) {
    console.log("Relationship not found for cid:", cid);
    return;
  }

  console.log("Sending notification", relationship.pid);

  const parentUser = await User.findById(relationship.pid);
  if (parentUser && parentUser.deviceToken) {
    const message = {
      notification: {
        title: "Geofence Alert",
        body: "Your child has left the designated area.",
      },
      token: parentUser.deviceToken,
    };

    admin
      .messaging()
      .send(message)
      .then((response) => console.log("Notification sent to parent:", response))
      .catch((error) => console.error("Error sending notification:", error));
  }
}

// store-location route
router.post("/store-location", async (req, res) => {
  const { cid, coordinates } = req.body;

  try {
    const locationUpdate = {
      cid,
      coordinates,
      timestamp: new Date(),
    };

    console.log(req.body);

    const options = { upsert: true, new: true, setDefaultsOnInsert: true };
    const updatedLocation = await LocationModel.findOneAndUpdate(
      { cid: cid },
      locationUpdate,
      options
    );

    // Check for geofence breaches for the updated location
    await checkForGeofenceBreaches(cid);

    res.status(200).json({
      message: "Location stored or updated successfully",
      location: updatedLocation,
    });
  } catch (error) {
    console.error("Store Location error:", error);
    res.status(500).json({ error: error.message });
  }
});

router.get("/fetch-child-location", async (req, res) => {
  const { pid } = req.query; // Assuming pid is passed as a query parameter

  try {
    // Fetch the cid for the given pid from Relationship
    const relationship = await Relationship.findOne({ pid: pid });
    if (!relationship) {
      return res.status(404).json({ message: "Relationship not found" });
    }
    const cid = relationship.cid;

    console.log(cid, pid);

    const latestLocation = await LocationModel.findOne({ cid: cid });

    console.log(latestLocation);
    if (!latestLocation) {
      return res
        .status(404)
        .json({ message: "Location data not found for this child" });
    }

    res.status(200).json({
      message: "Location fetched successfully",
      location: latestLocation,
    });
  } catch (error) {
    console.error("Fetch Child Location error:", error);
    res.status(500).json({ error: error.message });
  }
});

router.post("/update-geofence-coordinates", async (req, res) => {
  const { pid, geofenceCoordinates } = req.body;

  console.log(geofenceCoordinates);

  try {
    // Find the cid using pid
    const relationship = await Relationship.findOne({ pid: pid });
    if (!relationship) {
      return res.status(404).json({ message: "Relationship not found" });
    }
    const cid = relationship.cid;

    // Update the location with geofence coordinates
    const updatedLocation = await LocationModel.findOneAndUpdate(
      { cid: cid },
      { $set: { geofenceCordinates: geofenceCoordinates } },
      { new: true }
    );

    if (!updatedLocation) {
      return res
        .status(404)
        .json({ message: "Location data not found for this child" });
    }

    res.status(200).json({
      message: "Geofence coordinates updated successfully",
      location: updatedLocation,
    });
  } catch (error) {
    console.error("Update Geofence Coordinates error:", error);
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
