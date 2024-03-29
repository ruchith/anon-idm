package org.ruchith.research.idm.idp.db;

import it.unisa.dia.gas.jpbc.Element;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.ruchith.ae.base.AEParameters;
import org.ruchith.research.idm.IdentityClaimDefinition;

/**
 * Database connection and access helper methods.
 * 
 * @author Ruchith Fernando
 *
 */
public class Database {

	private static Database db;
	private static Connection con;
	private static final String DB_NAME = "idp";

	private Database(String host, String user, String password)
			throws Exception {
		String url = "jdbc:mysql://" + host + ":3306/" + DB_NAME;
		con = DriverManager.getConnection(url, user, password);
	}

	/**
	 * Return the singleton instance of the database.
	 * 
	 * @param host DB Host
	 * @param user DB Username
	 * @param password DB Password
	 * @return A {@link Database} instance
	 * @throws Exception
	 */
	public static Database getInstance(String host, String user, String password)
			throws Exception {
		if (db == null) {
			db = new Database(host, user, password);
		}
		return db;
	}

	/**
	 * Insert the given {@link IdentityClaimDefinition} instance into the 
	 * Claim_Definition table.
	 * @param claimDef {@link IdentityClaimDefinition} instance to insert.
	 * @throws Exception
	 */
	public void storeClaimDefinition(IdentityClaimDefinition claimDef)
			throws Exception {
		ObjectNode json = claimDef.getParams().serializeJSON();
		byte[] paramsJsonBytes = json.toString().getBytes();
		String sql = "INSERT INTO Claim_Definition VALUES('"
				+ claimDef.getName() + "','" + claimDef.getDescription()
				+ "','" + new String(Base64.encode(claimDef.getMasterKey().toBytes())) + "','"
				+ new String(Base64.encode(paramsJsonBytes)) + "','"
				+ claimDef.getB64Hash() + "','" + claimDef.getB64Sig() + "','"
				+ new String(Base64.encode(claimDef.getCert().getEncoded())) + "'," + "NOW())";
		
		con.createStatement().execute(sql);
	}

	/**
	 * Return all claim definitions.
	 * 
	 * @return
	 */
	public ResultSet getAllClaimDefinitions() throws Exception {
		String sql = "SELECT * FROM Claim_Definition";
		return con.createStatement().executeQuery(sql);
	}
	
	/**
	 * Fetch the given claim definition.
	 * @param name Name of the claim definition.
	 * @return 
	 * @throws Exception
	 */
	public IdentityClaimDefinition getClaimDefinition(String name) throws Exception {
		String sql = "SELECT * FROM Claim_Definition WHERE Name = '" + name + "'";
		ResultSet rs = con.createStatement().executeQuery(sql);
		if(rs.next()) {
			String desc = rs.getString("Description");
			String mk = rs.getString("PrivateKey");
			String pubParams = rs.getString("PublicParams");
			String dgst = rs.getString("Digest");
			String sig = rs.getString("Sig");
			String b64Cert = rs.getString("Cert");
			
			//b64 decode
			byte[] paramsJsonBytes = Base64.decode(pubParams);
			
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode paramsOn = (ObjectNode)mapper.readTree(new String(paramsJsonBytes));
			AEParameters params = new AEParameters(paramsOn);
			
			Element mkElem = params.getPairing().getG1().newElement();
			mkElem.setFromBytes(Base64.decode(mk.getBytes()));
			
			IdentityClaimDefinition claimDef = new IdentityClaimDefinition(name, params, mkElem);
			claimDef.setDescription(desc);
			claimDef.setB64Hash(dgst);
			claimDef.setB64Sig(sig);
			
			ByteArrayInputStream bais = new ByteArrayInputStream(Base64.decode(b64Cert));
			claimDef.setCert(CertificateFactory.getInstance("X.509")
					.generateCertificate(bais));
			
			return claimDef;
		} else {
			return null;
		}
		
	}
	
	/**
	 * Store user claim issuance information. 
	 * @param claimName Name of the claim.
	 * @param user User name
	 * @param r Random number created for the user's private key
	 * @param anonId
	 * @throws Exception
	 */
	public void storeClaim(String claimName, String user, Element r, Element anonId)
			throws Exception {

		String rB64 = new String(Base64.encode(r.toBytes()));
		String anonIdB64 = new String(Base64.encode(anonId.toBytes()));
		
		String sql = "INSERT INTO Claim (ClaimName, UserName, UserRandom, UserAnonId) " +
				"VALUES ('" + claimName + "','" + user + "','" + rB64 + "','" + anonIdB64 + "')";
		
		con.createStatement().execute(sql);
	}
	
	/**
	 * Get the given user's pub key certificate value 
	 * @param user User name
	 * @return Stored certificate as a string or null if user not present
	 * @throws Exception
	 */
	public String getUserCertValue(String user) throws Exception {
		String sql = "SELECT PubKeyCertificate FROM User where Name='" + user +"'";
		ResultSet rs = con.createStatement().executeQuery(sql);
		if(rs.next()) {
			return rs.getString("PubKeyCertificate");
		} else {
			return null;
		}
	}
	
	/**
	 * Get the given user's pub key certificate value by cert fingerprint.
	 * @param fpr Base64 encoded certificate fingerprint value.
	 * @return Stored certificate as a string or null if user not present
	 * @throws Exception
	 */
	public String getUserCertValueByFpr(String fpr) throws Exception {
		String sql = "SELECT PubKeyCertificate FROM User where PubKeyCertificateFpr='" + fpr +"'";
		ResultSet rs = con.createStatement().executeQuery(sql);
		if(rs.next()) {
			return rs.getString("PubKeyCertificate");
		} else {
			return null;
		}
	}
	
	/**
	 * Create new user entry.
	 * @param user User name
	 * @param certFpr Certificate fingerprint
	 * @param cert B64 encoded certificate
	 * @throws Exception
	 */
	public void addUserEntry(String user, String certFpr, String cert) throws Exception {
		String sql = "INSERT INTO User(Name, " +
				"PubKeyCertificateFpr, PubKeyCertificate) VALUES" +
				"('" + user + "','" + certFpr + "','" + cert + "')";
		con.createStatement().execute(sql);
	}
}
