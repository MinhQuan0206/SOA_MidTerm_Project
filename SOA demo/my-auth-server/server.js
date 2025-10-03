require('dotenv').config(); // load .env
const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const bcrypt = require('bcryptjs');
const User = require('./models/User');

const app = express();
app.use(cors());             // cho phép CORS
app.use(express.json());     // parse JSON body

const MONGO_URI = process.env.MONGO_URI || 'mongodb+srv://ThaoNhi:241105@cluster0.3uefbl7.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0';

mongoose.connect(MONGO_URI, { useNewUrlParser: true, useUnifiedTopology: true })
  .then(()=> console.log('MongoDB connected'))
  .catch(err => console.error('MongoDB error', err));

// Register endpoint
app.post('/register', async (req, res) => {
  const { username, password } = req.body;
  if (!username || !password) return res.json({ success: false, message: 'Thiếu username hoặc password' });

  try {
    const exist = await User.findOne({ username });
    if (exist) return res.json({ success: false, message: 'Username đã tồn tại' });

    const salt = await bcrypt.genSalt(10);
    const hash = await bcrypt.hash(password, salt);

    const user = new User({ username, passwordHash: hash });
    await user.save();
    return res.json({ success: true, message: 'Đăng ký thành công' });
  } catch (err) {
    console.error(err);
    return res.status(500).json({ success: false, message: 'Lỗi server' });
  }
});

// Login endpoint
app.post('/login', async (req, res) => {
  const { username, password } = req.body;
  if (!username || !password) return res.json({ success: false, message: 'Thiếu username hoặc password' });

  try {
    const user = await User.findOne({ username });
    if (!user) return res.json({ success: false, message: 'Sai tên hoặc mật khẩu' });

    const match = await bcrypt.compare(password, user.passwordHash);
    if (!match) return res.json({ success: false, message: 'Sai tên hoặc mật khẩu' });

    return res.json({ success: true, message: 'Đăng nhập thành công' });
  } catch (err) {
    console.error(err);
    return res.status(500).json({ success: false, message: 'Lỗi server' });
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, ()=> console.log(`Server chạy trên ${PORT}`));
