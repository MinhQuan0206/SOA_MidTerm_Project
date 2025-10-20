const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
  username: { type: String, required: true, unique: true },
  passwordHash: { type: String, required: true },
  email: { 
    type: String, 
    required: true, 
    unique: true, 
    lowercase: true, 
    match: [/^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/, 'Vui lòng nhập email hợp lệ'] 
  },
  createdAt: { type: Date, default: Date.now },
  activeSessions: { type: [String], default: [] } // Đảm bảo mặc định là mảng rỗng
});

module.exports = mongoose.model('User', userSchema);