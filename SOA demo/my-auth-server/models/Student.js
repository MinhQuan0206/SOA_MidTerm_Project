const mongoose = require('mongoose');

const studentSchema = new mongoose.Schema({
  username: { type: String, required: true, unique: true }, // Liên kết với user
  studentId: { type: String, required: true, unique: true },
  name: { type: String, required: true },
  courses: [{
    course_id: String,
    name: String,
    credits: Number,
    fee: Number
  }],
  tuitionFee: { type: Number, default: 0 } // Tổng học phí
});

module.exports = mongoose.model('Student', studentSchema);