const express = require('express');
const studentController = require('../controllers/studentController');

const router = express.Router();

router.get('/student/info', studentController.getStudentInfo);
router.get('/student/courses', studentController.getCourses);
router.get('/student/search', studentController.searchStudent);

module.exports = router;