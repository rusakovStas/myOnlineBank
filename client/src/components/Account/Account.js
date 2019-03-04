import React from "react";
import PropTypes from "prop-types";
import {
	Collapse,
	CardBody,
	Card,
	CardHeader,
	Button,
	Row,
	Col
} from "reactstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { Typeahead } from "react-bootstrap-typeahead";

class Account extends React.Component {
	state = {
		openTransaction: false,
		confirm: false,
		transaction: false,
		edite: false,
		block: false
	};

	constructor(props) {
		super(props);
		// create a ref to store the button DOM element
		this.inputForName = React.createRef();
	}

	componentDidUpdate(prevProps, prevState) {
		if (!prevState.edite && this.state.edite) {
			this.focus();
		}
	}

	handleFocus = event => {
		event.target.select();
	};

	focus = () => {
		this.inputForName.current.focus();
	};

	toggleTransaction = () => {
		this.setState({
			transaction: true,
			openTransaction: !this.state.openTransaction && this.props.open,
			confirm: true
		});
	};

	toggleEdit = () => {
		this.setState({
			edite: true,
			confirm: true
		});
	};

	toggleBlock = () => {
		this.setState({
			block: true,
			confirm: true
		});
	};

	decline = () => {
		if (this.state.block) {
			this.setState({
				confirm: false,
				block: false
			});
		}
		if (this.state.edite) {
			this.setState({
				confirm: false,
				edite: false
			});
		}
		if (this.state.transaction) {
			this.setState({
				confirm: false,
				transaction: false,
				openTransaction: false
			});
		}
	};

	accept = () => {
		if (this.state.block) {
			this.setState({
				confirm: false,
				block: false
			});
		}
		if (this.state.edite) {
			this.setState({
				confirm: false,
				edite: false
			});
		}
		if (this.state.transaction) {
			this.setState({
				confirm: false,
				transaction: false,
				openTransaction: false
			});
		}
	};

	render() {
		const { account, open, toggle, suggestions } = this.props;
		const { openTransaction, confirm, edite } = this.state;
		return (
			<Card className="text-center">
				{(!!account.name || edite) && (
					<CardHeader>
						<input
							value={account.name}
							disabled={this.state.edite === false}
							ref={this.inputForName}
							onFocus={this.handleFocus}
							className="border-0 form-input form-control"
						/>
					</CardHeader>
				)}
				<CardBody onClick={() => toggle(account.index)}>
					<div className="alert alert-primary" role="alert">
						{account.number}
					</div>
				</CardBody>

				<Collapse isOpen={open}>
					<Collapse isOpen={openTransaction}>
						<FontAwesomeIcon icon="arrow-down" size="2x" />
						<CardBody>
							<Typeahead
								labelKey="name"
								multiple={false}
								options={suggestions}
								placeholder="Type a email"
								onFocus={this.handleFocus}
								className="mb-2"
							/>
						</CardBody>
					</Collapse>

					{!confirm ? (
						<Row>
							<Col xs="6" sm="4">
								<Button
									size="lg"
									block
									color="danger"
									className="mb-2"
									onClick={this.toggleBlock}
								>
									Block
								</Button>
							</Col>
							<Col xs="6" sm="4">
								<Button
									size="lg"
									block
									color="success"
									onClick={this.toggleEdit}
								>
									Edite
								</Button>
							</Col>
							<Col sm="4">
								<Button
									size="lg"
									block
									color="primary"
									onClick={this.toggleTransaction}
								>
									Transaction
								</Button>
							</Col>
						</Row>
					) : (
						<Row>
							<Col xs="6">
								<Button
									size="lg"
									block
									color="success"
									onClick={this.accept}
								>
									Accept
								</Button>
							</Col>
							<Col xs="6">
								<Button
									size="lg"
									block
									color="danger"
									onClick={this.decline}
								>
									Decline
								</Button>
							</Col>
						</Row>
					)}
				</Collapse>
			</Card>
		);
	}
}

Account.propTypes = {
	transaction: PropTypes.func.isRequired,
	decline: PropTypes.func.isRequired,
	edite: PropTypes.func.isRequired,
	account: PropTypes.shape({
		index: PropTypes.number.isRequired,
		name: PropTypes.string,
		number: PropTypes.string.isRequired
	}).isRequired,
	suggestions: PropTypes.object.isRequired,
	open: PropTypes.bool.isRequired,
	toggle: PropTypes.func.isRequired
};

export default Account;
