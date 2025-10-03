const crypto = require('crypto'); // Để tạo OTP ngẫu nhiên
const nodemailer = require('nodemailer');
const Account = require('../models/Account');
const Student = require('../models/Student');
const Transaction = require('../models/Transaction');
const Otp = require('../models/Otp');

// Cấu hình nodemailer
const transporter = nodemailer.createTransport({
  service: 'gmail',
  auth: {
    user: process.env.EMAIL_USER,
    pass: process.env.EMAIL_PASS
  }
});

// Tạo OTP ngẫu nhiên
const generateOtp = () => crypto.randomBytes(3).toString('hex').toUpperCase(); // OTP 6 ký tự

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

    // Tạo OTP
    const otp = generateOtp();

    // Lưu OTP
    const otpDoc = new Otp({ username, otp });
    await otpDoc.save();

    // Gửi OTP đến email (giả định email từ username hoặc thêm trường email)
    const mailOptions = {
      from: process.env.EMAIL_FROM,
      to: 'kiettran112520@gmail.com', // Giả định email là username@gmail.com, bạn có thể thay bằng email thật
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
  const { username, otp, studentId, isSelf } = req.body;
  if (!username || !otp || (isSelf === false && !studentId)) 
    return res.json({ success: false, message: 'Thiếu thông tin' });

  try {
    // Kiểm tra OTP
    const otpDoc = await Otp.findOne({ username, otp });
    if (!otpDoc) return res.json({ success: false, message: 'OTP sai hoặc hết hạn' });

    // Xác định targetStudentId
    const targetStudentId = isSelf ? (await Student.findOne({ username }))?.studentId : studentId;
    if (!targetStudentId) return res.json({ success: false, message: 'Không tìm thấy sinh viên' });

    // Tìm student và lấy tuitionFee
    const student = await Student.findOne({ studentId: targetStudentId });
    if (!student) return res.json({ success: false, message: 'Không tìm thấy sinh viên' });
    if (student.tuitionFee === 0) return res.json({ success: false, message: 'Sinh viên này đã thanh toán học phí rồi' });

    // Lấy amount = tuitionFee
    const amount = student.tuitionFee;

    // Tìm account
    const account = await Account.findOne({ username });
    if (!account) return res.json({ success: false, message: 'Không tìm thấy tài khoản' });
    if (account.balance < amount) return res.json({ success: false, message: 'Số dư không đủ' });

    // Thanh toán
    account.balance -= amount;
    await account.save();

    student.tuitionFee = 0;
    await student.save();

    const transaction = new Transaction({ username, studentId: targetStudentId, amount, status: 'Thành công' });
    await transaction.save();

    // Xóa OTP sau khi thành công
    await Otp.deleteOne({ _id: otpDoc._id });

    return res.json({ success: true, message: 'Thanh toán thành công' });
  } catch (err) {
    console.error(err);
    return res.status(500).json({ success: false, message: 'Lỗi server' });
  }
};