require("dotenv").config();
const express = require("express");
const http = require("http"); // Import http module
const socketIo = require("socket.io"); // Import socket.io
const cors = require("cors");
const mongoose = require("mongoose");
const loginRoutes = require("./loginRoutes");
const relationshipRoutes = require("./relationshipRoutes");
const alertRoutes = require("./alertRoutes");
const serviceAccount =
  "./guardianteen-3f1b5-firebase-adminsdk-fglys-254d167587.json";
const admin = require("firebase-admin");
const MONGO_URI_STRING = process.env.MONGO_URI;
console.log("string", serviceAccount);

// Replace '/path/to/serviceAccountKey.json' with the path to your Firebase service account key file

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

mongoose.connect(process.env.MONGO_URI, {
  useNewUrlParser: true,
  useUnifiedTopology: true,
});

const app = express();
app.use(cors());
app.use(express.json());
app.use("/", loginRoutes);
app.use("/", relationshipRoutes);
app.use("/", alertRoutes);

const server = http.createServer(app); // Create an HTTP server
const io = socketIo(server); // Initialize socket.io on the server

// Handle WebSocket connections
io.on("connection", (socket) => {
  console.log("A user connected to WebSocket");

  // You can add WebSocket event handlers here for real-time communication

  // Handle disconnection
  socket.on("disconnect", () => {
    console.log("A user disconnected from WebSocket");
  });
});

const PORT = process.env.PORT || 5001;
server.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});
