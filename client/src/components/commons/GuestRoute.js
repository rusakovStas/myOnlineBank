import React from "react";
import { Route, Redirect } from "react-router-dom";
import PropTypes from "prop-types";
import { connect } from "react-redux";

const GuestRoute = ({ isAuthentifacated, component: Component, ...rest }) => (
	<Route
		{...rest}
		render={props =>
			!isAuthentifacated ? (
				<Component {...props} />
			) : (
				<Redirect to="/home" />
			)
		}
	/>
);

GuestRoute.propTypes = {
	component: PropTypes.func.isRequired,
	isAuthentifacated: PropTypes.bool.isRequired
};

function mapStateToProps(store) {
	return {
		isAuthentifacated: !!store.user.access_token
	};
}

export default connect(mapStateToProps)(GuestRoute);
