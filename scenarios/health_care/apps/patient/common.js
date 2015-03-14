console.log("Including common.js");

//var db = require('../db');
var fs = require('fs');
var jars_dir = fs.readFileSync('./jars_dir', 'utf8').trim();
console.log("Reading jars_dir: " + jars_dir);

var java = require("java");
java.classpath.push(jars_dir + "lib/base-1.0-SNAPSHOT.jar");
java.classpath.push(jars_dir + "lib/bcprov-jdk16-1.46.jar");
java.classpath.push(jars_dir + "lib/commons-io-1.3.2.jar");
java.classpath.push(jars_dir + "lib/jackson-core-asl-1.9.4.jar");
java.classpath.push(jars_dir + "lib/jackson-jaxrs-1.9.4.jar");
java.classpath.push(jars_dir + "lib/jackson-mapper-asl-1.9.4.jar");
java.classpath.push(jars_dir + "lib/jackson-mrbean-1.9.4.jar");
java.classpath.push(jars_dir + "lib/jackson-xc-1.9.4.jar");
java.classpath.push(jars_dir + "lib/jpbc-api-1.1.0.jar");
java.classpath.push(jars_dir + "lib/jpbc-crypto-1.1.0.jar");
java.classpath.push(jars_dir + "lib/jpbc-pbc-1.1.0.jar");
java.classpath.push(jars_dir + "lib/jpbc-plaf-1.1.0.jar");
java.classpath.push(jars_dir + "lib/junit-3.8.1.jar");
java.classpath.push(jars_dir + "lib/mysql-connector-java-5.1.22.jar");
java.classpath.push(jars_dir + "lib/idp-1.0-SNAPSHOT.jar");
java.classpath.push(jars_dir + "healthcare-1.0-SNAPSHOT.jar");

var config_dir = fs.readFileSync('./config_dir', 'utf8').trim();
console.log("Reading config_dir: " + config_dir);

var IdentityManager = java.import('org.ruchith.research.idm.idp.IdentityManager');
var idm = new IdentityManager(config_dir);
idm.testing();

exports.idm = idm;
exports.java = java;
