import { render, screen } from "@testing-library/react";
import * as React from "react";

import { ToggleColorMode } from "./ToggleColorMode";

describe("ToggleColorMode component", () => {
  const toggleColorMode = () => {
    setMode((prev) => (prev === "dark" ? "light" : "dark"));
  };

  it("Renders light mode", () => {
    render(<ToggleColorMode mode="light" toggleColorMode={toggleColorMode} />);
    expect(screen.getByTestId("ModeNightRoundedIcon")).toBeInTheDocument();
  });

  it("Renders dark mode", () => {
    render(<ToggleColorMode mode="dark" toggleColorMode={toggleColorMode} />);
    expect(screen.getByTestId("WbSunnyRoundedIcon")).toBeInTheDocument();
  });
});
