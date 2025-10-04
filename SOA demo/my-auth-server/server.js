require('dotenv').config(); // load .env
const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');

const app = express();
app.use(cors());             // cho phép CORS
app.use(express.json());     // parse JSON body

const MONGO_URI = process.env.MONGO_URI || 'mongodb+srv://ThaoNhi:241105@cluster0.3uefbl7.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0';

mongoose.connect(MONGO_URI, { useNewUrlParser: true, useUnifiedTopology: true })
  .then(()=> console.log('MongoDB connected'))
  .catch(err => console.error('MongoDB error', err));

// Thêm routes
app.use('/', require('./routes/authRoutes'));
app.use('/', require('./routes/studentRoutes'));
app.use('/', require('./routes/transactionRoutes'));
app.use('/', require('./routes/paymentRoutes'));

const PORT = process.env.PORT || 3000;
app.listen(PORT, ()=> console.log(`Server chạy trên ${PORT}`));