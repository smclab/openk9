import React, { useEffect, useState, CSSProperties } from "react";

const frames = ["⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"];

interface UnicodeSpinnerProps {
	text?: string;
	interval?: number;
	sx?: CSSProperties;
}

const UnicodeSpinner: React.FC<UnicodeSpinnerProps> = ({ text, interval = 80, sx = {} }) => {
	const [frameIndex, setFrameIndex] = useState<number>(0);

	useEffect(() => {
		const id = setInterval(() => {
			setFrameIndex((prev) => (prev + 1) % frames.length);
		}, interval);
		return () => clearInterval(id);
	}, [interval]);

	return (
		<>
			{text} {frames[frameIndex]}
		</>
	);
};

export default UnicodeSpinner;
