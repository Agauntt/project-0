package com.iron_bank.dao.impl;

import java.beans.Statement;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.dbutil.OracleConnection;
import com.iron_bank.dao.UserDAO;
import com.iron_bank.exceptions.BusinessException;
import com.iron_bank.model.User;
import com.iron_bank.model.UserDetails;

import sun.util.logging.resources.logging;

public class UserDaoImpl implements UserDAO{

	// This method takes in a UserDetails object and passes it through the register_User procedure to 
	// create a new user account with a sequentially generated account number 
	@Override
	public UserDetails registerDetails(UserDetails uDetails) throws BusinessException {
		try (Connection connection = OracleConnection.getConnection()){
			String sql = "{call register_User(?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
			CallableStatement callableStatement = connection.prepareCall(sql);
			callableStatement.setString(2, uDetails.getUserName());
			callableStatement.setString(3, uDetails.getPassWord());
			callableStatement.setInt(4,  uDetails.getPin());
			callableStatement.setString(5,  uDetails.getFirstName());
			callableStatement.setString(6,  uDetails.getLastName());
			callableStatement.setString(7, uDetails.getContact());
			callableStatement.setString(8,  uDetails.getEmail());
			callableStatement.setString(9,  uDetails.getAddress());
			callableStatement.setString(10,  uDetails.getCity());
			callableStatement.setString(11,  uDetails.getState());
			callableStatement.setInt(12,  uDetails.getZip());
			callableStatement.setDate(13, new java.sql.Date(uDetails.getDob().getTime()));
			callableStatement.setInt(14, uDetails.getSsn());
			callableStatement.registerOutParameter(1, java.sql.Types.NUMERIC);
			
			callableStatement.execute();
			uDetails.setAcctId(callableStatement.getInt(1));
		} catch (ClassNotFoundException | SQLException e) {
			System.out.println(e);
			throw new BusinessException("Internal error occured");
		}
		return uDetails;
	}

	
	// Takes in a user object that is partially generated by the user attempting to log in, if the credentials match an existing user,
	// the rest of the object is retrieved from the DB and passed back, allowing the user to access account features within the main
	// menu
	@Override
	public User authUser(User user) throws BusinessException {
		try(Connection connection = OracleConnection.getConnection()) {
			String sql = "SELECT acct_id, pin FROM users WHERE username = ? AND password = ?";
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, user.getUserName());
			preparedStatement.setString(2, user.getPassWord());
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				user.setAcctId(resultSet.getLong("acct_id"));
				user.setPin(resultSet.getInt("pin"));
			} else {
				System.out.println("User not found");
			}
		} catch (ClassNotFoundException | SQLException e) {
			System.out.println(e);
		}
		return user;
	}


	@Override
	public UserDetails displayDetails(long userID) throws BusinessException {
		UserDetails uDetails = new UserDetails();
		try(Connection connection = OracleConnection.getConnection()) {
			String sql = "SELECT * FROM user_info where user_id = ?";
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setLong(1, userID);
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				uDetails.setAcctId(userID);
				uDetails.setFirstName(resultSet.getString("first_name"));
				uDetails.setLastName(resultSet.getString("last_name"));
				uDetails.setAddress(resultSet.getString("address"));
				uDetails.setCity(resultSet.getString("city"));
				uDetails.setState(resultSet.getString("state"));
				uDetails.setSsn(resultSet.getInt("ssn"));
				uDetails.setDob(resultSet.getDate("dob"));
				uDetails.setContact(resultSet.getString("contact"));
				uDetails.setEmail(resultSet.getString("email"));
			}
		} catch (ClassNotFoundException | SQLException e) {
			throw new BusinessException("Internal error occured...Please try again later");
		}
		return uDetails;
	}

	

}
