const express = require('express');
const paymentController = require('../controllers/paymentController');

const router = express.Router();

router.post('/payment/requestPayTuition', paymentController.requestPayTuition);
router.post('/payment/verifyOtpAndPay', paymentController.verifyOtpAndPay);

module.exports = router;