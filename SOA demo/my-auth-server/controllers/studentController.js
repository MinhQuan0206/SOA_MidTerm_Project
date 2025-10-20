const Student = require('../models/Student');
const User = require('../models/User');
exports.getStudentInfo = async (req, res) => {
  const username = req.query.username;
  try {
    const student = await Student.findOne({ username });
    if (!student) return res.json({ success: false, message: 'Không tìm thấy sinh viên' });

    // Lấy email từ User dựa trên username
    const user = await User.findOne({ username });
    const email = user?.email || '';

    // Trả về dữ liệu Student với email bổ sung
    const response = {
      username: student.username,
      studentId: student.studentId,
      name: student.name,
      courses: student.courses,
      tuitionFee: student.tuitionFee,
      email: email // Thêm email vào response
    };
    return res.json(response);
  } catch (err) {
    console.error(err);
    return res.status(500).json({ success: false, message: 'Lỗi server' });
  }
};

exports.getCourses = async (req, res) => {
  const studentId = req.query.student_id;
  try {
    const student = await Student.findOne({ studentId });
    if (!student) return res.json({ success: false, message: 'Không tìm thấy sinh viên' });
    // Trả về courses rỗng nếu tuitionFee = 0
    const courses = student.tuitionFee === 0 ? [] : student.courses;
    return res.json(courses);
  } catch (err) {
    console.error(err);
    return res.status(500).json({ success: false, message: 'Lỗi server' });
  }
};

exports.searchStudent = async (req, res) => {
  const studentId = req.query.student_id;
  try {
    const student = await Student.findOne({ studentId });
    if (!student) return res.json({ success: false, message: 'Không tìm thấy sinh viên' });

    // Lấy email từ User dựa trên username
    const user = await User.findOne({ username: student.username });
    const email = user?.email || '';

    // Trả về dữ liệu Student với email bổ sung
    const response = {
      username: student.username,
      studentId: student.studentId,
      name: student.name,
      courses: student.courses,
      tuitionFee: student.tuitionFee,
      email: email // Thêm email vào response
    };
    return res.json(response);
  } catch (err) {
    console.error(err);
    return res.status(500).json({ success: false, message: 'Lỗi server' });
  }
};