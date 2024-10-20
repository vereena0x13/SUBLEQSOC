module top(
	input wire t_clk,
	input wire t_rst,

	inout wire [7:0] t_uart_data,
	input wire t_uart_txe,
	input wire t_uart_rxf,
	output wire t_uart_wr,
	output wire t_uart_rd
);

	reg rst_s0 = 1'b0;
	reg rst_s1 = 1'b0;
	always @(posedge t_clk) rst_s0 <= t_rst;
	always @(posedge t_clk) rst_s1 <= rst_s0;


	reg [32:0] rst_tmr = 32'h0;
	wire por = rst_tmr != 1000000;
	always @(posedge t_clk) begin
		if(por) begin
			rst_tmr <= rst_tmr + 1;
		end
	end

	wire rst = ~rst_s1 | por;


	reg txe_s0 = 1'b1;
	reg txe_s1 = 1'b1;
	always @(posedge t_clk) txe_s0 <= t_uart_txe;
	always @(posedge t_clk) txe_s1 <= txe_s0;
	reg rxf_s0 = 1'b1;
	reg rxf_s1 = 1'b1;
	always @(posedge t_clk) rxf_s0 <= t_uart_rxf;
	always @(posedge t_clk) rxf_s1 <= rxf_s0;


	wire [7:0] uart_do;
	wire uart_wr;
	wire uart_rd;

    assign t_uart_data = t_uart_wr ? 8'bZZZZZZZZ : uart_do;
	assign t_uart_wr = ~uart_wr;
	assign t_uart_rd = ~uart_rd;
    

    SubleqSOC soc(
        .clk(t_clk),
        .reset(rst),
        .io_uart_data_i(t_uart_data),
		.io_uart_data_o(uart_do),
		.io_uart_txe(txe_s1),
		.io_uart_rxf(rxf_s1),
		.io_uart_wr(uart_wr),
		.io_uart_rd(uart_rd)
    );


endmodule