const express = require('express');
const paymentController = require('../controllers/paymentController');

const router = express.Router();

router.post('/payment/payTuition', paymentController.payTuition);

module.exports = router;