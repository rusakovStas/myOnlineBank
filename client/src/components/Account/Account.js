import React from "react";
import PropTypes from "prop-types";
import {
	Collapse,
	CardText,
	Card,
	CardHeader,
	Button,
	Row,
	Col,
	CardImg,
	CardImgOverlay,
	CardTitle,
	Alert
} from "reactstrap";
import cover from "./cover.jpg";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { Typeahead } from "react-bootstrap-typeahead";
import FormButton from "../commons/FormButton";

class Account extends React.Component {
	state = {
		openTransaction: false,
		confirm: false,
		transaction: false,
		edite: false,
		block: false,
		loading: false
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
		const { openTransaction, confirm, edite, block, loading } = this.state;
		return (
			<Card className="text-center text-white account-item shadow p-2">
				{(!!account.name || edite) && (
					<CardTitle>
						<input
							value={account.name}
							disabled={this.state.edite === false}
							ref={this.inputForName}
							onFocus={this.handleFocus}
							className="border-0 form-input form-control input-in-header text-white"
						/>
					</CardTitle>
				)}
				<CardText onClick={() => toggle(account.index)}>
					<large>
						<p>
							<b className="text-monospace">
								Amount: {account.amount}
								<FontAwesomeIcon icon="ruble-sign" size="1x" />
							</b>
						</p>
					</large>

					<p className="text-monospace card-text text-right pl-1 mb-0">
						<small>
							<b>{account.number}</b>
						</small>
					</p>

					<p className="card-text text-right pl-1 mt-0">
						<small className="text-muted">
							Owner: {account.owner}
						</small>
					</p>
				</CardText>

				<Collapse isOpen={open || confirm}>
					<Collapse isOpen={openTransaction}>
						<FontAwesomeIcon icon="arrow-down" size="2x" />
						<CardText className="pl-0 pr-0">
							<Typeahead
								labelKey="name"
								multiple={false}
								options={suggestions}
								placeholder="Type a email"
								className="mb-2"
							/>
						</CardText>
					</Collapse>
					<Collapse isOpen={block}>
						<CardText className="text-danger">
							<b>Are you sure you want to block the account?</b>
						</CardText>
					</Collapse>
					<div className="m-1">
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
										{!!account.name || account.name === ""
											? "Edite"
											: "Name"}
									</Button>
								</Col>
								<Col sm="4">
									<Button
										size="lg"
										block
										color="purple"
										onClick={this.toggleTransaction}
									>
										Transaction
									</Button>
								</Col>
							</Row>
						) : (
							<Row>
								<Col xs="6">
									<FormButton
										loading={loading}
										variant="success"
										block
										size="lg"
										submit={this.accept}
									>
										Accept
									</FormButton>
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
					</div>
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
