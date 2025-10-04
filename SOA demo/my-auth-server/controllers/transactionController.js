const Transaction = require('../models/Transaction');

exports.getTransactions = async (req, res) => {
  const username = req.query.username;
  try {
    const transactions = await Transaction.find({ username });
    return res.json(transactions);
  } catch (err) {
    console.error(err);
    return res.status(500).json({ success: false, message: 'Lỗi server' });
  }
};