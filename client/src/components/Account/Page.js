import React from "react";
import PropTypes from "prop-types";
import { connect } from "react-redux";
import AccountForm from "./Form";
import {
	getAllAccounts,
	getMyAccounts,
	// createAccount,
	deleteAccount
	// editeAccount,
} from "../../actions/accounts";
import api from "../../api/api";

class AccountPage extends React.Component {
	state = { suggestions: [] };

	componentDidMount() {
		if (this.props.hasRoleAdmin) {
			this.props.getAllAccounts(this.props.currentUser);
		} else {
			this.props.getMyAccounts(this.props.currentUser);
		}
		api.account
			.getSuggestions()
			.then(res => this.setState({ suggestions: res }));
	}

	render() {
		return (
			<AccountForm
				accounts={this.props.accounts}
				suggestions={this.state.suggestions}
				transaction={api.account.createTransaction}
				decline={this.props.deleteAccount}
				currentUser={this.props.currentUser}
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
	editeAccount: PropTypes.func.isRequired,
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
		// createAccount,
		deleteAccount,
		getMyAccounts
		// editeAccount,
	}
)(AccountPage);
