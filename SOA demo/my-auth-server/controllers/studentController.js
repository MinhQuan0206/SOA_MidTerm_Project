const Student = require('../models/Student');

exports.getStudentInfo = async (req, res) => {
  const username = req.query.username;
  try {
    const student = await Student.findOne({ username });
    if (!student) return res.json({ success: false, message: 'Không tìm thấy sinh viên' });
    return res.json(student);
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
    return res.json(student.courses);
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
    return res.json(student);
  } catch (err) {
    console.error(err);
    return res.status(500).json({ success: false, message: 'Lỗi server' });
  }
};