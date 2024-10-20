// Generator : SpinalHDL v1.10.2a    git head : a348a60b7e8b6a455c72e1536ec3d74a2ea16935
// Component : SubleqSOC
// Git hash  : 6430be60ddf808551852b807081678e5de83f076

`timescale 1ns/1ps

module SubleqSOC (
  input  wire [7:0]    io_uart_data_i,
  output wire [7:0]    io_uart_data_o,
  input  wire          io_uart_txe,
  input  wire          io_uart_rxf,
  output wire          io_uart_wr,
  output wire          io_uart_rd,
  input  wire          clk,
  input  wire          reset
);

  reg        [15:0]   subleq_1_io_bus_rdata;
  wire       [15:0]   mem_spinal_port1;
  wire       [15:0]   subleq_1_io_bus_addr;
  wire       [15:0]   subleq_1_io_bus_wdata;
  wire                subleq_1_io_bus_write;
  wire       [15:0]   _zz_mem_port;
  wire       [15:0]   _zz_mem_port_1;
  wire                when_SubleqSOC_l33;
  wire       [15:0]   _zz_io_bus_rdata;
  (* ram_style = "distributed" *) reg [15:0] mem [0:65535];

  assign _zz_mem_port = subleq_1_io_bus_addr;
  assign _zz_mem_port_1 = subleq_1_io_bus_wdata;
  always @(posedge clk) begin
    if(subleq_1_io_bus_write) begin
      mem[_zz_mem_port] <= _zz_mem_port_1;
    end
  end

  assign mem_spinal_port1 = mem[_zz_io_bus_rdata];
  Subleq subleq_1 (
    .io_bus_addr  (subleq_1_io_bus_addr[15:0] ), //o
    .io_bus_rdata (subleq_1_io_bus_rdata[15:0]), //i
    .io_bus_wdata (subleq_1_io_bus_wdata[15:0]), //o
    .io_bus_write (subleq_1_io_bus_write      ), //o
    .clk          (clk                        ), //i
    .reset        (reset                      )  //i
  );
  assign io_uart_data_o = 8'h0;
  assign io_uart_wr = 1'b0;
  assign io_uart_rd = 1'b0;
  assign when_SubleqSOC_l33 = ($signed(subleq_1_io_bus_addr) < $signed(16'h0));
  always @(*) begin
    if(when_SubleqSOC_l33) begin
      subleq_1_io_bus_rdata = 16'h0;
    end else begin
      subleq_1_io_bus_rdata = mem_spinal_port1;
    end
  end

  assign _zz_io_bus_rdata = subleq_1_io_bus_addr;

endmodule

module Subleq (
  output wire [15:0]   io_bus_addr,
  input  wire [15:0]   io_bus_rdata,
  output reg  [15:0]   io_bus_wdata,
  output wire          io_bus_write,
  input  wire          clk,
  input  wire          reset
);

  reg        [15:0]   regs_a;
  reg        [15:0]   regs_b;
  reg        [15:0]   regs_c;
  reg        [15:0]   regs_ip;
  reg        [15:0]   b_addr;
  reg        [15:0]   b_wdata;
  reg                 b_write;
  reg        [15:0]   t0;
  reg        [15:0]   t1;
  wire       [15:0]   sub;
  wire                br;
  reg        [3:0]    state;
  wire                when_Subleq_l98;

  assign io_bus_addr = b_addr;
  always @(*) begin
    if(b_write) begin
      io_bus_wdata = b_wdata;
    end else begin
      io_bus_wdata = 16'h0;
    end
  end

  assign io_bus_write = b_write;
  assign sub = ($signed(t1) - $signed(t0));
  assign br = ($signed(sub) <= $signed(16'h0));
  assign when_Subleq_l98 = (state == 4'b1100);
  always @(posedge clk or posedge reset) begin
    if(reset) begin
      regs_a <= 16'h0;
      regs_b <= 16'h0;
      regs_c <= 16'h0;
      regs_ip <= 16'h0;
      regs_ip <= 16'h0;
      b_addr <= 16'h0;
      b_wdata <= 16'h0;
      b_write <= 1'b0;
      t0 <= 16'h0;
      t1 <= 16'h0;
      state <= 4'b0000;
    end else begin
      case(state)
        4'b0000 : begin
          b_addr <= regs_ip;
        end
        4'b0001 : begin
          regs_a <= io_bus_rdata;
          regs_ip <= ($signed(regs_ip) + $signed(16'h0001));
        end
        4'b0010 : begin
          b_addr <= regs_ip;
        end
        4'b0011 : begin
          regs_b <= io_bus_rdata;
          regs_ip <= ($signed(regs_ip) + $signed(16'h0001));
        end
        4'b0100 : begin
          b_addr <= regs_ip;
        end
        4'b0101 : begin
          regs_c <= io_bus_rdata;
          regs_ip <= ($signed(regs_ip) + $signed(16'h0001));
        end
        4'b0110 : begin
          b_addr <= regs_a;
        end
        4'b0111 : begin
          t0 <= io_bus_rdata;
        end
        4'b1000 : begin
          b_addr <= regs_b;
        end
        4'b1001 : begin
          t1 <= io_bus_rdata;
        end
        4'b1010 : begin
          b_addr <= regs_b;
          b_wdata <= sub;
          b_write <= 1'b1;
        end
        4'b1011 : begin
          b_write <= 1'b0;
          if(br) begin
            regs_ip <= regs_c;
          end
        end
        default : begin
        end
      endcase
      if(when_Subleq_l98) begin
        state <= 4'b0000;
      end else begin
        state <= (state + 4'b0001);
      end
    end
  end


endmodule
