package com.brianstempin.vindiniumclient.bot.advanced;

import com.brianstempin.vindiniumclient.dto.GameState;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class AdvancedGameState {
    private Map<GameState.Position, Mine> mines;
    private Map<GameState.Position, Pub> pubs;
    private Map<GameState.Position, GameState.Hero> heroesByPosition;
    private Map<Integer, GameState.Hero> heroesById;
    private Map<GameState.Position, Vertex> boardGraph;
    private GameState.Hero me;

    public AdvancedGameState() {}

    /**
     * Creates an AdvancedGameState from a GameState
     * @param gameState
     */
    public AdvancedGameState(GameState gameState) {
        boardGraph = new HashMap<>();
        mines = new HashMap<>();
        pubs = new HashMap<>();
        heroesById = new HashMap<>();
        heroesByPosition = new HashMap<>();

        // Hero stuffs
        for(GameState.Hero currentHero : gameState.getGame().getHeroes()) {
            this.heroesByPosition.put(currentHero.getPos(), currentHero);
            this.heroesById.put(currentHero.getId(), currentHero);
        }

        this.me = gameState.getHero();

        // Build the graph sans edges
        GameState.Board board = gameState.getGame().getBoard();
        for (int row = 0; row < board.getSize(); row++) {
            for (int col = 0; col < board.getSize(); col++) {
                GameState.Position pos = new GameState.Position(col, row);

                Vertex v = new Vertex(pos, new LinkedList<Vertex>());
                int tileStart = row * board.getSize() * 2 + (col * 2);
                String tileValue = board.getTiles().substring(tileStart, tileStart + 1 + 1);

                // We do nothing with tiles that are barriers
                if (tileValue.equals("##"))
                    continue;

                this.boardGraph.put(v.getPosition(), v);

                // If its a mine or tavern, we treat it differently
                if (tileValue.startsWith("$")) {
                    String owner = tileValue.substring(1);
                    Mine mine;
                    if (owner.equals("-")) {
                        mine = new Mine(pos, null);
                    } else {
                        int ownerId = Integer.parseInt(owner);
                        mine = new Mine(pos, this.heroesById.get(ownerId));
                    }

                    this.mines.put(pos, mine);
                } else if (tileValue.equals("[]")) {
                    Pub pub = new Pub(pos);
                    this.pubs.put(pos, pub);
                }
            }
        }

        // Add in the edges
        // This graph doesn't take into account players because they move.  That is done elsewhere.
        for (Vertex currentVertex : this.boardGraph.values()) {
            GameState.Position currentVertexPosition = currentVertex.getPosition();

            // Pubs and mines can't be passed through
            if(this.mines.containsKey(currentVertexPosition) || this.pubs.containsKey(currentVertexPosition))
                continue;

            for (int xDelta = -1; xDelta <= 1; xDelta += 2) {
                for (int yDelta = -1; yDelta <= 1; yDelta += 2) {
                    GameState.Position adjacentPosition = new GameState.Position(currentVertexPosition.getX() + xDelta,
                            currentVertexPosition.getY() + yDelta);

                    Vertex adjacentVertex = this.boardGraph.get(adjacentPosition);
                    if (adjacentVertex != null)
                        currentVertex.getAdjacentVertices().add(adjacentVertex);
                }
            }
        }
    }

    /**
     * Creates a new AdvancedGameState by taking he previous AdvancedGameState and updating is using a new GameState
     * @param oldGameState
     * @param updatedState
     */
    public AdvancedGameState(AdvancedGameState oldGameState, GameState updatedState) {

        // Copy the stuff we can just re-use
        this.boardGraph = oldGameState.getBoardGraph();
        this.pubs = oldGameState.getPubs();

        // Update the mines
        this.mines = oldGameState.getMines();
        for(Mine currentMine : this.mines.values()) {
            int tileStart = currentMine.getPosition().getY()
                    * updatedState.getGame().getBoard().getSize()
                    *  2 + (currentMine.getPosition().getX() * 2);
            // We don't want the whole tile; we want the second char
            String owner = updatedState.getGame().getBoard().getTiles().substring(tileStart + 1, tileStart + 1 + 1);
            Mine mine;
            if (owner.equals("-")) {
                mine = new Mine(currentMine.getPosition(), null);
            } else {
                int ownerId = Integer.parseInt(owner);
                mine = new Mine(currentMine.getPosition(), this.heroesById.get(ownerId));
            }

            this.mines.put(mine.getPosition(), mine);
        }

        // Re-build the hero maps
        for(GameState.Hero currentHero : updatedState.getGame().getHeroes()) {
            this.heroesByPosition.put(currentHero.getPos(), currentHero);
            this.heroesById.put(currentHero.getId(), currentHero);
        }
    }

    public AdvancedGameState(Map<GameState.Position, Mine> mines, Map<GameState.Position, Pub> pubs,
                             Map<GameState.Position, GameState.Hero> heroesByPosition, Map<Integer,
            GameState.Hero> heroesById, Map<GameState.Position, Vertex> boardGraph, GameState.Hero me) {
        this.mines = mines;
        this.pubs = pubs;
        this.heroesByPosition = heroesByPosition;
        this.heroesById = heroesById;
        this.boardGraph = boardGraph;
        this.me = me;
    }

    public Map<GameState.Position, Mine> getMines() {
        return mines;
    }

    public void setMines(Map<GameState.Position, Mine> mines) {
        this.mines = mines;
    }

    public Map<GameState.Position, Pub> getPubs() {
        return pubs;
    }

    public void setPubs(Map<GameState.Position, Pub> pubs) {
        this.pubs = pubs;
    }

    public Map<GameState.Position, GameState.Hero> getHeroesByPosition() {
        return heroesByPosition;
    }

    public void setHeroesByPosition(Map<GameState.Position, GameState.Hero> heroesByPosition) {
        this.heroesByPosition = heroesByPosition;
    }

    public Map<Integer, GameState.Hero> getHeroesById() {
        return heroesById;
    }

    public void setHeroesById(Map<Integer, GameState.Hero> heroesById) {
        this.heroesById = heroesById;
    }

    public Map<GameState.Position, Vertex> getBoardGraph() {
        return boardGraph;
    }

    public void setBoardGraph(Map<GameState.Position, Vertex> boardGraph) {
        this.boardGraph = boardGraph;
    }

    public GameState.Hero getMe() {
        return me;
    }

    public void setMe(GameState.Hero me) {
        this.me = me;
    }
}
