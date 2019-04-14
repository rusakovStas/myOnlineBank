import React from "react";
import {
    Collapse,
    Navbar,
    NavbarToggler,
    NavbarBrand,
    Nav,
    NavItem,
    NavLink
} from "reactstrap";
import { PropTypes } from "prop-types";
import { Link } from "react-router-dom";
import { connect } from "react-redux";
import * as actions from "../../actions/auth";

class TopNavigationBar extends React.Component {
    state = {};

    toggle = () => this.setState({ isOpen: !this.state.isOpen });

    render() {
        const { logout } = this.props;
        return (
            <Navbar color="dark" dark expand="md">
                <NavbarBrand tag={Link} to="/home">
                    Home
                </NavbarBrand>
                <NavbarToggler onClick={this.toggle} />
                <Collapse isOpen={this.state.isOpen} navbar>
                    <Nav className="ml-auto" navbar>
                        <NavItem>
                            <NavLink tag={Link} to="/accounts">
                                Accounts
                            </NavLink>
                        </NavItem>
                        {this.props.hasRoleAdmin && (
                            <NavItem>
                                <NavLink tag={Link} to="/admin">
                                    Admin
                                </NavLink>
                            </NavItem>
                        )}
                        <NavItem>
                            <NavLink tag={Link} to="/" onClick={() => logout()}>
                                logout
                            </NavLink>
                        </NavItem>
                    </Nav>
                </Collapse>
            </Navbar>
        );
    }
}

TopNavigationBar.propTypes = {
    logout: PropTypes.func.isRequired,
    hasRoleAdmin: PropTypes.bool.isRequired
};

function mapStateToProps(store) {
    return {
        hasRoleAdmin:
            !!store.user.roles &&
            !!store.user.roles.find(element => element === "ROLE_ADMIN")
    };
}

export default connect(
    mapStateToProps,
    { logout: actions.logout }
)(TopNavigationBar);
