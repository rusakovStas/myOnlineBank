import React from "react";
import { Route, Redirect } from "react-router-dom";
import PropTypes from "prop-types";
import { connect } from "react-redux";

const AdminRoute = ({
	isAuthentifacated,
	hasRoleAdmin,
	component: Component,
	...rest
}) => (
	<Route
		{...rest}
		render={props =>
			isAuthentifacated && hasRoleAdmin ? (
				<Component {...props} />
			) : (
				<Redirect to="/" />
			)
		}
	/>
);

AdminRoute.propTypes = {
	component: PropTypes.func.isRequired,
	isAuthentifacated: PropTypes.bool.isRequired
};

function mapStateToProps(store) {
	return {
		isAuthentifacated: !!store.user.access_token,
		hasRoleAdmin:
			!!store.user.roles &&
			!!store.user.roles.find(element => element === "ROLE_ADMIN")
	};
}

export default connect(mapStateToProps)(AdminRoute);
