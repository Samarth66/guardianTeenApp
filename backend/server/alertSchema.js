const mongoose = require("mongoose");

const alertSchema = new mongoose.Schema({
  cid: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "User",
    required: true,
  },
  type: {
    type: String,
    required: true,
  },
  description: {
    type: String,
    required: false,
  },

  time: {
    type: Date,
    default: Date.now,
    required: true,
  },
  location: {
    type: String,
    required: false,
  },
});

module.exports = mongoose.model("Alert", alertSchema);
