import React from "react";
import PropTypes from "prop-types";
import "./FormButton.scss";

class FormButton extends React.Component {
	constructor(props) {
		super(props);
		// create a ref to store the button DOM element
		this.button = React.createRef();
	}

	state = {
		width: 0
	};

	componentDidMount() {
		this.setState({
			width: this.button.current.getBoundingClientRect().width
		});
	}

	computedClass = () => {
		let className = "btn";
		if (this.props.block) {
			className += " btn-block";
		}
		if (this.props.outline) {
			className += ` btn-outline-${this.props.variant}`;
		} else {
			className += ` btn-${this.props.variant}`;
		}
		if (this.props.size !== "") {
			className += ` btn-${this.props.size}`;
		}
		return className;
	};

	render() {
		return (
			<button
				type="submit"
				className={this.computedClass()}
				disabled={this.props.loading}
				onClick={this.props.submit}
				ref={this.button}
				style={{ minWidth: `${this.state.width}px` }}
			>
				{this.props.loading ? (
					<span>
						<span className="spinner">
							<span className="bounce1" />
							<span className="bounce2" />
							<span className="bounce3" />
						</span>
					</span>
				) : (
					<span>{this.props.children}</span>
				)}
			</button>
		);
	}
}

FormButton.propTypes = {
	children: PropTypes.string.isRequired,
	variant: PropTypes.string,
	outline: PropTypes.bool,
	size: PropTypes.string,
	block: PropTypes.bool,
	loading: PropTypes.bool,
	submit: PropTypes.func.isRequired
};

FormButton.defaultProps = {
	variant: "primary",
	outline: false,
	size: "",
	block: false,
	loading: false
};

export default FormButton;
