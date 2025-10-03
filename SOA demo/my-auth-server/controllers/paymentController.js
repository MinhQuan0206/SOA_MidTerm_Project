const Account = require('../models/Account');
const Student = require('../models/Student');
const Transaction = require('../models/Transaction');

exports.payTuition = async (req, res) => {
  const { username, studentId, amount } = req.body;
  if (!username || !studentId || !amount) return res.json({ success: false, message: 'Thiếu thông tin' });

  try {
    const account = await Account.findOne({ username });
    if (!account) return res.json({ success: false, message: 'Không tìm thấy tài khoản' });
    if (account.balance < amount) return res.json({ success: false, message: 'Số dư không đủ' });

    const student = await Student.findOne({ studentId });
    if (!student) return res.json({ success: false, message: 'Không tìm thấy sinh viên' });

    // Cập nhật số dư
    account.balance -= amount;
    await account.save();

    // Cập nhật học phí sinh viên
    student.tuitionFee = 0; // Giả định thanh toán toàn bộ
    await student.save();

    // Thêm giao dịch
    const transaction = new Transaction({ username, studentId, amount, status: 'Thành công' });
    await transaction.save();

    return res.json({ success: true, message: 'Thanh toán thành công' });
  } catch (err) {
    console.error(err);
    return res.status(500).json({ success: false, message: 'Lỗi server' });
  }
};