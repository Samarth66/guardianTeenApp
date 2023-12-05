const mongoose = require("mongoose");

const relationshipSchema = new mongoose.Schema({
  pid: {
    type: String,
    required: true,
  },
  cid: {
    type: String,
    required: true,
  },
});

const Relationship = mongoose.model("Relationship", relationshipSchema);
module.exports = Relationship;
