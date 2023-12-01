const express = require("express");
const router = express.Router(); // Create a router instance\

const User = require("./userSchema"); // Import your User model

router.get("/test", (req, res) => {
  res.send("Test route is working");
});

router.post("/updateDeviceToken", async (req, res) => {
  const { email, deviceToken } = req.body;
  console.log(req.body);
  try {
    const user = await User.findOneAndUpdate(
      { email: email },
      { deviceToken: deviceToken },
      { new: true }
    );

    if (!user) {
      return res.status(404).send({ message: "User not found" });
    }

    res
      .status(200)
      .send({ message: "Device token updated successfully", user });
  } catch (error) {
    res.status(500).send({ message: "Error updating device token", error });
  }
});

router.post("/login", async (req, res) => {
  const { email, password, userType } = req.body;
  console.log("req received", email, password, userType);
  try {
    const existingUser = await User.findOne({
      email: email,
      password: password,
      userType: userType,
    });
    if (existingUser) {
      return res
        .status(201)
        .json({ id: existingUser._id, name: existingUser.name });
    } else {
      const userCheck = await User.findOne({
        email: email,
      });

      if (userCheck) {
        return res.status(401).json({ message: "Wrong Credentials" });
      } else {
        return res.status(400).json({ message: "User doesn't Exists" });
      }
    }
  } catch (error) {
    console.log("login error", error);
    res.status(500).json({ error: error.message });
  }
});

router.post("/signup", async (req, res) => {
  const { name, email, password, userType } = req.body;
  console.log("req received", name, email, password, userType);
  try {
    const existingUser = await User.findOne({ email: email });
    console.log("got", existingUser);
    if (existingUser) {
      return res.status(400).json({ message: "Email already exists" });
    }

    const newUser = new User({ name, email, password, userType });
    await newUser.save();
    res.status(201).json({ message: "User created successfully" });
  } catch (error) {
    console.error("Signup error:", error);
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
