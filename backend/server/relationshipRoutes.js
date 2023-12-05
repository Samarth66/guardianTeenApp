const express = require("express");
const router = express.Router(); // Create a router instance
const User = require("./userSchema"); // Import your User model
const Relationship = require("./relationshipSchema");
// Add Child
router.post("/add_child", async (req, res) => {
  const { pid, cid } = req.body;
  try {
    // Check if the child (cid) exists in the Users database with userType "child"
    const childExists = await User.exists({ _id: cid, userType: "child" });

    if (!childExists) {
      return res
        .status(400)
        .json({ error: "Child not found in the database." });
    }

    const newRelationship = new Relationship({ pid, cid });
    await newRelationship.save();
    res.status(201).json({
      message: "Child added successfully",
      relationship: newRelationship,
    });
  } catch (error) {
    console.error("Add Child error:", error);
    res.status(500).json({ error: error.message });
  }
});

// Fetch Child
router.post("/fetch_child", async (req, res) => {
  const { pid } = req.body;
  try {
    const children = await Relationship.find({ pid: pid });
    res.status(200).json(children.map((child) => child.cid));
  } catch (error) {
    console.error("Fetch Child error:", error);
    res.status(500).json({ error: error.message });
  }
});

// Fetch Parent
router.post("/fetch_parent", async (req, res) => {
  const { cid } = req.body;
  try {
    const parent = await Relationship.findOne({ cid: cid });
    if (!parent) {
      return res.status(404).json({ message: "Parent not found" });
    }
    res.status(200).json({ pid: parent.pid });
  } catch (error) {
    console.error("Fetch Parent error:", error);
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
