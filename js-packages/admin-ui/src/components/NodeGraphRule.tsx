import { Handle, Position } from "react-flow-renderer";

export default function NodeGraphRule(props: any) {
  const { data } = props;

  return (
    <div
      style={{
        width: "120px",
        height: "60px",
        background: "blue",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        borderRadius: "8px",
      }}
    >
      <Handle type="target" position={Position.Top} isConnectable={true} />
      {data.label}
      <Handle type="source" position={Position.Bottom} isConnectable={true} />
    </div>
  );
}
