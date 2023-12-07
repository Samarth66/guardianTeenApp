const mongoose = require("mongoose");

const locationSchema = new mongoose.Schema({
  cid: {
    type: String,
    required: true,
  },
  coordinates: {
    type: [Number], // [longitude, latitude]
    required: false,
  },

  geofenceCordinates: {
    type: [Number],
    required: false,
  },

  lastGeofenceState: {
    type: Boolean,
    required: false,
    default: true, // Assuming initially inside the geofence
  },
  timestamp: {
    type: Date,
    default: Date.now,
  },
});

module.exports = mongoose.model("Location", locationSchema);
