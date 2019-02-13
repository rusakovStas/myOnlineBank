import React from "react";
import { Route, Redirect } from "react-router-dom";
import PropTypes from "prop-types";
import { connect } from "react-redux";

const UserRoute = ({ isAuthentifacated, component: Component, ...rest }) => (
	<Route
		{...rest}
		render={props =>
			isAuthentifacated ? <Component {...props} /> : <Redirect to="/" />
		}
	/>
);

UserRoute.propTypes = {
	component: PropTypes.func.isRequired,
	isAuthentifacated: PropTypes.bool.isRequired
};

function mapStateToProps(store) {
	return {
		isAuthentifacated: !!store.user.access_token
	};
}

export default connect(mapStateToProps)(UserRoute);
