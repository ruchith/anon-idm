var mysql      = require('mysql');
var connection = mysql.createConnection({
	host     : 'localhost',
	user     : 'root',
	password : '',
	database : 'idp',
	multipleStatements: true
});
connection.connect();

exports.getAllClaimDefs = function(cb) {
	
	connection.query('SELECT  Name, Description, PublicParams, Digest, Sig, Cert, DateCreated  FROM Claim_Definition', function(err, rows, fields) {
	  if (err) throw err;
	  cb(rows);
	});
	
}


exports.getClaimDetails = function(name, cb) {

	connection.query("SELECT Name, Description, PublicParams, Digest, Sig, Cert, DateCreated FROM Claim_Definition WHERE Name='" + name + "'", function(err, rows, fields) {
		if (err) throw err;
		cb(rows[0]);
	});

}

exports.getUserDetails = function(cb) {

	connection.query("SELECT * FROM User", function(err, rows, fields) {
		if (err) throw err;
		cb(rows);
	});

}