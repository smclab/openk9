import React, { Suspense, useEffect, useState } from "react";
import { Canvas, useLoader } from "react-three-fiber";
import { softShadows } from "@react-three/drei";
import { useTrail } from "@react-spring/core";
import { a } from "@react-spring/three";
import * as THREE from "three";
import styles from "./searchesAnimation.module.css";

softShadows({});

const types = {
  File: { texI: 0, tex: "texture/File.png", size: [1, 1 / (97 / 129)] },
  Document: { texI: 1, tex: "texture/Document.png", size: [1, 1 / (97 / 129)] },
  Email: { texI: 2, tex: "texture/Email.png", size: [1, 1 / (129 / 97)] },
  Presentation: {
    texI: 3,
    tex: "texture/Presentation.png",
    size: [1, 1 / (129 / 97)],
  },
  Spreadsheet: {
    texI: 4,
    tex: "texture/Spreadsheet.png",
    size: [1, 1 / (97 / 129)],
  },
  Contact: { texI: 5, tex: "texture/Contact.png", size: [1, 1] },
};

const queries = [
  {
    query: "Sales Report",
    results: [
      "Presentation",
      "Spreadsheet",
      "Document",
      "Document",
      "Spreadsheet",
    ],
  },
  {
    query: "John Doe",
    results: ["Contact", "Email", "Email", "Document", "Email"],
  },
  {
    query: "ACME Inc",
    results: ["Contact", "Contact", "Document", "Document", "Spreadsheet"],
  },
  {
    query: "backend.js",
    results: ["File", "File", "File", "Document", "File"],
  },
  {
    query: "Project Slides",
    results: [
      "Presentation",
      "Presentation",
      "Presentation",
      "Presentation",
      "Presentation",
    ],
  },
  {
    query: "Review Year 2020",
    results: [
      "Presentation",
      "Spreadsheet",
      "Document",
      "Document",
      "Spreadsheet",
    ],
  },
  {
    query: "Emails from John Doe",
    results: ["Email", "Email", "Email", "Email", "Contact"],
  },
];

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));
const randomArr = (arr) => arr[Math.floor(Math.random() * arr.length)];

const noMaterial = new THREE.MeshBasicMaterial({
  transparent: true,
  opacity: 0,
  depthWrite: false,
});

function FileCard({ x, y, z, w, h, texture }) {
  return (
    <a.mesh
      position-x={x}
      position-y={y}
      position-z={z}
      castShadow
      material={[
        noMaterial,
        noMaterial,
        noMaterial,
        noMaterial,
        new THREE.MeshBasicMaterial({
          transparent: true,
          color: "#eee",
          map: texture,
        }),
        noMaterial,
        noMaterial,
        noMaterial,
      ]}
    >
      <boxBufferGeometry args={[w, h, 0.01]} />
    </a.mesh>
  );
}

function Scene({ trail, query }) {
  const textures = useLoader(
    THREE.TextureLoader,
    Object.keys(types).map((key) => types[key].tex),
  );

  if (!query) return null;
  const files = query.results.map((key) => types[key]);

  return (
    <>
      <directionalLight
        castShadow
        position={[2.5, 8, 5]}
        intensity={1}
        shadow-mapSize-width={1024}
        shadow-mapSize-height={1024}
        shadow-camera-far={50}
        shadow-camera-left={-10}
        shadow-camera-right={10}
        shadow-camera-top={10}
        shadow-camera-bottom={-10}
      />

      <FileCard
        x={-1}
        y={trail[0].pos}
        z={-1}
        w={files[0].size[0]}
        h={files[0].size[1]}
        texture={textures[files[0].texI]}
      />
      <FileCard
        x={0}
        y={trail[1].pos}
        z={0}
        w={files[1].size[0]}
        h={files[1].size[1]}
        texture={textures[files[1].texI]}
      />
      <FileCard
        x={1}
        y={trail[2].pos}
        z={1}
        w={files[2].size[0]}
        h={files[2].size[1]}
        texture={textures[files[2].texI]}
      />
      <FileCard
        x={1}
        y={trail[3].pos}
        z={-1}
        w={files[3].size[0]}
        h={files[3].size[1]}
        texture={textures[files[3].texI]}
      />
      <FileCard
        x={-1}
        y={trail[4].pos}
        z={1}
        w={files[4].size[0]}
        h={files[4].size[1]}
        texture={textures[files[4].texI]}
      />

      <mesh rotation={[-Math.PI / 2, 0, 0]} castShadow receiveShadow>
        <planeBufferGeometry args={[20, 20]} />
        <shadowMaterial opacity={0.5} />
      </mesh>
      <mesh rotation={[-Math.PI / 2, 0, 0]}>
        <planeBufferGeometry args={[20, 20]} />
        <meshBasicMaterial />
      </mesh>
    </>
  );
}

function SearchIcon() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24">
      <g transform="translate(-317 -400)">
        <rect
          width="24"
          height="24"
          transform="translate(317 400)"
          fill="#fff"
        />
        <line
          x2="8"
          y2="8"
          transform="translate(330 412)"
          fill="none"
          stroke="#707070"
          strokeWidth="2"
        />
        <g
          transform="translate(320 403)"
          fill="#fff"
          stroke="#707070"
          strokeWidth="2"
        >
          <circle cx="8" cy="8" r="8" stroke="none" />
          <circle cx="8" cy="8" r="7" fill="none" />
        </g>
      </g>
    </svg>
  );
}

export function SearchesAnimation() {
  const [toggle, setToggle] = useState(false);
  const [query, setQuery] = useState(null);
  const trail = useTrail(5, { pos: toggle ? 1 : -1 });
  useEffect(() => {
    async function anim() {
      const query = randomArr(queries);
      setQuery(query);

      await sleep(500);
      setToggle(true);

      await sleep(4000);
      setToggle(false);
      await sleep(1000);
      anim();
    }
    anim();
  }, []);

  return (
    <div className={styles.root}>
      <div className={styles.anim}>
        <Canvas
          colorManagement={false}
          camera={{ position: [3, 4, 9], fov: 20 }}
          shadowMap
          invalidateFrameloop
          pixelRatio={[1, 2]}
          onCreated={({ camera }) => {
            camera.lookAt(0, 1, 0);
          }}
        >
          <Suspense fallback={null}>
            <Scene trail={trail} query={query} />
          </Suspense>
        </Canvas>
      </div>
      <div className={styles.searchField}>
        <SearchIcon />
        <span style={{ opacity: toggle ? 1 : 0 }}>{query && query.query}</span>
      </div>
    </div>
  );
}
