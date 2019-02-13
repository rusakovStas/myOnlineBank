import React from "react";
import PropTypes from "prop-types";
import { connect } from "react-redux";
import UserForm from "./Form";
import { getUsers, addNewUser, deleteUser } from "../../actions/admin";

class UserPage extends React.Component {
	state = {};

	componentDidMount() {
		this.props.getUsers();
	}

	render() {
		const { users } = this.props;
		return (
			<UserForm
				add={this.props.addNewUser}
				delete={this.props.deleteUser}
				users={users}
			/>
		);
	}
}

UserPage.propTypes = {
	users: PropTypes.arrayOf(PropTypes.object).isRequired,
	getUsers: PropTypes.func.isRequired,
	addNewUser: PropTypes.func.isRequired,
	deleteUser: PropTypes.func.isRequired
};

function mapStateToProps(state) {
	return {
		users: state.users
	};
}

export default connect(
	mapStateToProps,
	{ getUsers, addNewUser, deleteUser }
)(UserPage);
