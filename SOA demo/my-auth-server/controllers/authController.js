const bcrypt = require('bcryptjs');
const User = require('../models/User');
const Account = require('../models/Account');
const crypto = require('crypto');

exports.register = async (req, res) => {
  const { username, password } = req.body;
  if (!username || !password) return res.json({ success: false, message: 'Thiếu username hoặc password' });

  try {
    const exist = await User.findOne({ username });
    if (exist) return res.json({ success: false, message: 'Username đã tồn tại' });

    const salt = await bcrypt.genSalt(10);
    const hash = await bcrypt.hash(password, salt);

    const user = new User({ username, passwordHash: hash, activeSessions: [] });
    await user.save();

    const account = new Account({ username, balance: 0 });
    await account.save();

    return res.json({ success: true, message: 'Đăng ký thành công' });
  } catch (err) {
    console.error(err);
    return res.status(500).json({ success: false, message: 'Lỗi server' });
  }
};

exports.login = async (req, res) => {
  const { username, password } = req.body;
  if (!username || !password) return res.json({ success: false, message: 'Thiếu username hoặc password' });

  try {
    const user = await User.findOne({ username });
    if (!user) return res.json({ success: false, message: 'Sai tên hoặc mật khẩu' });

    const match = await bcrypt.compare(password, user.passwordHash);
    if (!match) return res.json({ success: false, message: 'Sai tên hoặc mật khẩu' });

    // Kiểm tra nếu đã có phiên hoạt động
    if (user.activeSessions.length > 0) {
      return res.json({ success: false, message: 'Tài khoản đã được đăng nhập trên thiết bị khác' });
    }

    const sessionId = crypto.randomBytes(16).toString('hex');
    user.activeSessions = [sessionId];
    await user.save();

    return res.json({ success: true, message: 'Đăng nhập thành công', sessionId });
  } catch (err) {
    console.error(err);
    return res.status(500).json({ success: false, message: 'Lỗi server' });
  }
};

exports.logout = async (req, res) => {
  const { username, sessionId } = req.body;
  if (!username || !sessionId) return res.json({ success: false, message: 'Thiếu username hoặc sessionId' });

  try {
    const user = await User.findOne({ username });
    if (!user) return res.json({ success: false, message: 'Không tìm thấy tài khoản' });

    user.activeSessions = user.activeSessions.filter(id => id !== sessionId); // Xóa sessionId cụ thể
    await user.save();

    return res.json({ success: true, message: 'Đăng xuất thành công' });
  } catch (err) {
    console.error(err);
    return res.status(500).json({ success: false, message: 'Lỗi server' });
  }
};