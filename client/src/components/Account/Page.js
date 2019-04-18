import React from "react";
import PropTypes from "prop-types";
import { connect } from "react-redux";
import AccountForm from "./Form";
import {
	getAllAccounts,
	getMyAccounts,
	createAccount,
	deleteAccount,
	editAccount
} from "../../actions/accounts";
import api from "../../api/api";

class AccountPage extends React.Component {
	componentDidMount() {
		if (this.props.hasRoleAdmin) {
			this.props.getAllAccounts(this.props.currentUser);
		} else {
			this.props.getMyAccounts(this.props.currentUser);
		}
	}

	render() {
		return (
			<AccountForm
				accounts={this.props.accounts}
				getSuggestions={api.account.getSuggestions}
				transaction={api.account.createTransaction}
				decline={this.props.deleteAccount}
				currentUser={this.props.currentUser}
				create={this.props.createAccount}
				edite={this.props.editAccount}
				hasRoleAdmin={this.props.hasRoleAdmin}
			/>
		);
	}
}

AccountPage.propTypes = {
	hasRoleAdmin: PropTypes.bool.isRequired,
	currentUser: PropTypes.string.isRequired,
	getAllAccounts: PropTypes.func.isRequired,
	getMyAccounts: PropTypes.func.isRequired,
	createAccount: PropTypes.func.isRequired,
	editAccount: PropTypes.func.isRequired,
	deleteAccount: PropTypes.func.isRequired,
	accounts: PropTypes.arrayOf.isRequired
};

function mapStateToProps(store) {
	return {
		hasRoleAdmin:
			!!store.user.roles &&
			!!store.user.roles.find(element => element === "ROLE_ADMIN"),
		currentUser: store.user.name,
		accounts: store.accounts
	};
}

export default connect(
	mapStateToProps,
	{
		getAllAccounts,
		createAccount,
		deleteAccount,
		getMyAccounts,
		editAccount
	}
)(AccountPage);
