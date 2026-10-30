package com.example.appmanutencao.adapter;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appmanutencao.R;
import com.example.appmanutencao.model.Mensagem;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MensagemViewHolder> {

    private List<Mensagem> listaMensagens;
    private String usuarioAtual; // Ex: "CLIENTE" ou "ADMIN"

    public ChatAdapter(List<Mensagem> listaMensagens, String usuarioAtual) {
        this.listaMensagens = listaMensagens;
        this.usuarioAtual = usuarioAtual;
    }

    @NonNull
    @Override
    public MensagemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mensagem, parent, false);
        return new MensagemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MensagemViewHolder holder, int position) {
        Mensagem msg = listaMensagens.get(position);
        holder.textMensagem.setText(msg.getTexto());
        holder.textRemetente.setText(msg.getRemetente());

        // Alinha à direita se a mensagem for do usuário atual, ou à esquerda se for do outro
        if (msg.getRemetente() != null && msg.getRemetente().equalsIgnoreCase(usuarioAtual)) {
            holder.layoutContainer.setGravity(Gravity.END);
        } else {
            holder.layoutContainer.setGravity(Gravity.START);
        }
    }

    @Override
    public int getItemCount() {
        return listaMensagens != null ? listaMensagens.size() : 0;
    }

    public void atualizarMensagens(List<Mensagem> novasMensagens) {
        this.listaMensagens = novasMensagens;
        notifyDataSetChanged();
    }

    static class MensagemViewHolder extends RecyclerView.ViewHolder {
        TextView textRemetente, textMensagem;
        LinearLayout layoutContainer;

        public MensagemViewHolder(@NonNull View itemView) {
            super(itemView);
            textRemetente = itemView.findViewById(R.id.textRemetente);
            textMensagem = itemView.findViewById(R.id.textMensagem);
            layoutContainer = itemView.findViewById(R.id.layoutContainer);
        }
    }
}