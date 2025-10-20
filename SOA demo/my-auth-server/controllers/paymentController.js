const mongoose = require('mongoose');
const crypto = require('crypto');
const nodemailer = require('nodemailer');
const Account = require('../models/Account');
const Student = require('../models/Student');
const Transaction = require('../models/Transaction');
const Otp = require('../models/Otp');
const User = require('../models/User'); // Import User model

// Cấu hình nodemailer
const transporter = nodemailer.createTransport({
  service: 'gmail',
  auth: {
    user: process.env.EMAIL_USER,
    pass: process.env.EMAIL_PASS
  }
});

// Tạo OTP ngẫu nhiên
const generateOtp = () => {
  const digits = '0123456789';
  let otp = '';
  for (let i = 0; i < 6; i++) {
    otp += digits[Math.floor(Math.random() * 10)];
  }
  return otp;
};

exports.requestPayTuition = async (req, res) => {
  const { username, studentId, isSelf } = req.body;
  if (!username || (isSelf === false && !studentId)) 
    return res.json({ success: false, message: 'Thiếu thông tin' });

  try {
    // Xác định targetStudentId
    const targetStudentId = isSelf ? (await Student.findOne({ username }))?.studentId : studentId;
    if (!targetStudentId) return res.json({ success: false, message: 'Không tìm thấy sinh viên' });

    // Tìm student và lấy tuitionFee
    const student = await Student.findOne({ studentId: targetStudentId });
    if (!student) return res.json({ success: false, message: 'Không tìm thấy sinh viên' });
    if (student.tuitionFee === 0) return res.json({ success: false, message: 'Sinh viên này đã thanh toán học phí rồi' });
    console.log("Student:", student);

    // Tạo OTP
    const otp = generateOtp();

    // Lưu OTP
    const otpDoc = new Otp({ username, otp });
    await otpDoc.save();

    // Lấy email từ User
    const user = await User.findOne({ username });
    if (!user || !user.email) {
      return res.json({ success: false, message: 'Không tìm thấy email của tài khoản' });
    }
    const userEmail = user.email;
    // Thêm session ID 
    const sessionId = req.headers['session-id'] || crypto.randomBytes(16).toString('hex');
    user.activeSessions = user.activeSessions || [];
    if (!user.activeSessions.includes(sessionId)) {
      user.activeSessions.push(sessionId);
      await user.save();
    }
    // Gửi OTP đến email của tài khoản
    const mailOptions = {
      from: process.env.EMAIL_FROM,
      to: userEmail,
      subject: 'Mã OTP Thanh Toán Học Phí',
      text: `Mã OTP của bạn là ${otp}. Mã này có hiệu lực trong 5 phút.`
    };
    await transporter.sendMail(mailOptions);

    return res.json({ success: true, message: 'OTP đã gửi đến email, vui lòng kiểm tra' });
  } catch (err) {
    console.error(err);
    return res.status(500).json({ success: false, message: 'Lỗi server' });
  }
};

exports.verifyOtpAndPay = async (req, res) => {
  const { username, otp, studentId, isSelf, sessionId } = req.body;
  if (!username || !otp || (isSelf === false && !studentId)) 
    return res.json({ success: false, message: 'Thiếu thông tin' });

  const session = await mongoose.startSession();
  session.startTransaction();

  try {
    const user = await User.findOne({ username }).session(session);
    if (!user || !user.activeSessions.includes(sessionId)) {
      throw new Error('Phiên không hợp lệ');
    }
    // Kiểm tra OTP trong transaction
    const otpDoc = await Otp.findOne({ username, otp }).session(session);
    if ((new Date() - otpDoc.createdAt > 1 * 60 * 1000)) {
      throw new Error('OTP đã hết hạn');
    }
    if (!otpDoc) throw new Error('OTP sai. VUI LÒNG THỬ LẠI');

    // Xác định targetStudentId
    const targetStudentId = isSelf ? (await Student.findOne({ username }).session(session))?.studentId : studentId;
    if (!targetStudentId) throw new Error('Không tìm thấy sinh viên');

    // Kiểm tra lại student và tuitionFee
    const student = await Student.findOne({ studentId: targetStudentId }).session(session);
    if (!student) throw new Error('Không tìm thấy sinh viên');
    if (student.tuitionFee === 0) throw new Error('Sinh viên này đã thanh toán học phí rồi');

    // Lấy amount = tuitionFee
    const amount = student.tuitionFee;

    // Tìm account
    const account = await Account.findOne({ username }).session(session);
    if (!account) throw new Error('Không tìm thấy tài khoản');
    if (account.balance < amount) throw new Error('Số dư không đủ');

    // Thực hiện thanh toán
    account.balance -= amount;
    await account.save({ session });

    student.tuitionFee = 0;
    student.courses = [];
    await student.save({ session });

    const transaction = new Transaction({ username, studentId: targetStudentId, amount, status: 'Thành công' });
    await transaction.save({ session });

    // Xóa OTP
    await Otp.deleteOne({ _id: otpDoc._id }).session(session);

    await session.commitTransaction();

    // Gửi email xác nhận
    // const user = await User.findOne({ username }).session(session);
    if (user && user.email) {
      const mailOptions = {
        from: process.env.EMAIL_FROM,
        to: user.email,
        subject: 'Xác Nhận Thanh Toán Học Phí Thành Công',
        text: `Chào ${username},\n\nThanh toán học phí cho sinh viên ${targetStudentId} đã thành công!\n- Số tiền: ${amount} VNĐ\n- Thời gian: ${new Date().toLocaleString('vi-VN')}\n\nCảm ơn bạn đã sử dụng dịch vụ!`
      };
      await transporter.sendMail(mailOptions);
    }

    session.endSession();

    return res.json({ success: true, message: 'Thanh toán thành công' });
  } catch (err) {
    await session.abortTransaction();
    session.endSession();
    console.error(err);
    return res.json({ success: false, message: err.message });
  }
};

exports.getBalance = async (req, res) => {
  const { username } = req.query;
  if (!username) 
    return res.json({ success: false, message: 'Thiếu username' });

  try {
    const account = await Account.findOne({ username });
    if (!account) return res.json({ success: false, message: 'Không tìm thấy tài khoản' });

    return res.json({ success: true, message: 'Lấy số dư thành công', balance: account.balance });
  } catch (err) {
    console.error(err);
    return res.status(500).json({ success: false, message: 'Lỗi server' });
  }
};