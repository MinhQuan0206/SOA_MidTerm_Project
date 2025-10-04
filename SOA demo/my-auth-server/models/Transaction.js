const mongoose = require('mongoose');

const transactionSchema = new mongoose.Schema({
  username: { type: String, required: true },
  studentId: { type: String, required: true },
  amount: { type: Number, required: true },
  date: { type: Date, default: Date.now },
  status: { type: String, default: 'Thành công' } // Thành công, Thất bại, Đang xử lý
});

module.exports = mongoose.model('Transaction', transactionSchema);