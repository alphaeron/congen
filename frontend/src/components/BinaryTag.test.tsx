import { render, screen } from "@testing-library/react";
import * as React from "react";

import { BinaryTag } from "./BinaryTag";

describe("BinaryTag component", () => {
  it("Renders the correct on text", () => {
    render(<BinaryTag isOn={true} onText="on" offText="off" />);
    expect(screen.getByText("on")).toBeInTheDocument();
  });

  it("Renders the correct off text", () => {
    render(<BinaryTag isOn={false} onText="on" offText="off" />);
    expect(screen.getByText("off")).toBeInTheDocument();
  });
});
